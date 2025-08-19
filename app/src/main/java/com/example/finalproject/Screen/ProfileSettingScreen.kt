package com.example.finalproject.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.finalproject.ImageUploading.uploadImageToCloudinary
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.net.URL

@SuppressLint("InlinedApi")
@Composable
fun ProfileSettingScreen(){

    val context = LocalContext.current
    var imageRef by remember{ mutableStateOf<Uri?>(null)}
    var photoUrl by remember { mutableStateOf<String?>(SharedPref.getImage(context)) }
    var userName by remember{ mutableStateOf<String?>(SharedPref.getName(context))}
    var name by remember { mutableStateOf<String>(SharedPref.getName(context)) }


    var userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val databaseRef = FirebaseDatabase.getInstance()
        .getReference("users")
        .child(userId)

    LaunchedEffect(userId) {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newUrl = snapshot.child("imageUrl").getValue(String::class.java)
                val newUsername = snapshot.child("userName").getValue(String::class.java)
                val newName = snapshot.child("name").getValue(String::class.java)


                if(newUrl != null){
                    photoUrl = newUrl
                    SharedPref.saveImage(context,newUrl)
                }
                if(newUsername != null){
                    userName = newUsername
                    SharedPref.saveUsername(context,newUsername)
                }
                if(newName != null){
                    name = newName
                    SharedPref.saveName(context,newName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.e("Profile Update","Failed to fetch user data",error.toException())
            }

        })
    }







    val permissionToRequest = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        Manifest.permission.READ_MEDIA_IMAGES
    }else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if(uri != null){
            println("selected image ${uri}")
            imageRef = uri
        }else{
            println("no image selected")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        isGranted ->
        if(isGranted){
            println("permission granted")
            launcher.launch("image/*")
        }else{
            println("permission denied")
            Toast.makeText(context,"Permission required",Toast.LENGTH_SHORT).show()
        }
    }





    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        AsyncImage(
            model = photoUrl?: R.drawable.profile_image,
            contentDescription = "Profile image",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable {
                    val isGranted = ContextCompat.checkSelfPermission(
                        context,
                        permissionToRequest
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        launcher.launch("image/*")
                    } else {
                        permissionLauncher.launch(permissionToRequest)
                    }
                },
            contentScale = ContentScale.Crop
        )


        Button(
            onClick = {
                   if(imageRef == null){
                    Toast.makeText(context,"please select a photo",Toast.LENGTH_SHORT).show()
                }else{
                    println("final image uri before upload ${imageRef}")
                    uploadImageToCloudinary(
                        context,
                        imageRef!!,
                        "296176452574737",  
                        "qQPbMgW7Uih0pGZY4NqHjIqbfiI",
                        "dummy"
                    ){ url ->
                        if(url != null){
                            photoUrl = url
                            Log.d("cloudinary response from profile","image uploaded successfully : ${url}")

                            databaseRef.child("imageUrl").setValue(url)
                                .addOnSuccessListener {
                                    SharedPref.saveImage(context,url)
                                    Log.e("ProfileUpdate", "url updated with url : $url")
                                }
                                .addOnFailureListener{
                                    Log.e("ProfileUpdate", "Failed to update profile photo URL", it)
                                }

                        } else {
                            Log.d("cloudinary response from profile","Image upload failed") // Debugging log
                        }
                    }
                }
            } 
        ) {
            Text(
                text = "change profile photo"
            )
        }

        OutlinedTextField(
            value = userName ?:"",
            onValueChange = {userName = it},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                backgroundColor = MaterialTheme.colorScheme.background,
            )
        )
        Button(
            onClick = {
                databaseRef.child("userName").setValue(userName)
                    .addOnSuccessListener {
                        SharedPref.saveUsername(context,userName?:"Guest")
                        Log.e("Username update","username updated with ${userName}")
                    }
                    .addOnFailureListener{
                        Log.e("Username update","error in username update : ${it.message}")

                    }
            }
        ) {
            Text(
                text = "change username"
            )
        }

        OutlinedTextField(
            value = name ?:"",
            onValueChange = {name = it},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                backgroundColor = MaterialTheme.colorScheme.background,
            )
        )
        Button(
            onClick = {
                databaseRef.child("name").setValue(name)
                    .addOnSuccessListener {
                        SharedPref.saveName(context,name)
                        Log.e("name update","name updated with ${name}")
                    }
                    .addOnFailureListener{
                        Log.e("name update","error in name update : ${it.message}")

                    }
            }
        ) {
            Text(
                text = "change name"
            )
        }
    }
}