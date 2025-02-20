package com.example.finalproject.Screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.finalproject.ImageUploading.uploadImageToCloudinary
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.R
import com.example.finalproject.ViewModel.ThreadViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.contracts.contract

@Composable
fun addThread(navController: NavController,
              threadViewModel: ThreadViewModel){
    var context = LocalContext.current
//    var profileImg by remember {mutableStateOf("")}


    val isLoading by threadViewModel.isLoading.observeAsState(initial = false)
    var imageRef by remember{ mutableStateOf<Uri?>(null)}
    var caption by remember{ mutableStateOf("")}

    val requestToPermission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        Manifest.permission.READ_MEDIA_IMAGES
    }else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }


    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            uri ->
            imageRef = uri
        }

    var permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            isGranted ->
            if(isGranted){
                println("Permission granted") // Debugging log
                launcher.launch("image/*")
            }else{
                println("Permission denied") // Debugging log
                Toast.makeText(context, "Permission required to select image", Toast.LENGTH_LONG).show()
            }
        }


    if (isLoading) {
        // Show a loading indicator (e.g., CircularProgressIndicator)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }else {
        Scaffold(bottomBar = { myBottomBar(navController) }) { it ->
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add Thread",
                    style = TextStyle(
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    println("shared pref wali image : " + SharedPref.getImage(context))
                    AsyncImage(
                        model = SharedPref.getImage(context),
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.threads_log),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = SharedPref.getName(context),
                        style = TextStyle(
                            fontSize = 22.sp
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Write caption here..") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    trailingIcon = {
                        if (imageRef == null)
                            Icon(imageVector = Icons.Filled.AttachFile, contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clickable {
                                        var isGranted = ContextCompat.checkSelfPermission(
                                            context,
                                            requestToPermission
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (isGranted) {
                                            launcher.launch("image/*")
                                        } else {
                                            permissionLauncher.launch(requestToPermission)
                                        }
                                    })
                    })


                if (imageRef != null) {
                    AsyncImage(
                        model = imageRef, contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.Blue)
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))

                Button(onClick = {
                    if (imageRef == null && caption.isEmpty()) {
                        Toast.makeText(context, "make thread first", Toast.LENGTH_LONG).show()
                    } else {
                        if (imageRef != null) {
                            uploadImageToCloudinary(
                                context,
                                imageRef!!,
                                "296176452574737",
                                "qQPbMgW7Uih0pGZY4NqHjIqbfiI",
                                "dummy"
                            ) { url ->
                                threadViewModel.saveThread(
                                    FirebaseAuth.getInstance().currentUser?.uid,
                                    url,
                                    caption
                                ) {
                                    if (it) {
                                        Toast.makeText(
                                            context,
                                            "thread added successfully!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate(Screens.Home.route)
                                    }
                                }
                            }
                        } else {
                            threadViewModel.saveThread(
                                uid = FirebaseAuth.getInstance().currentUser?.uid,
                                thread = caption
                            ) {
                                if (it) {
                                    Toast.makeText(
                                        context,
                                        "thread added successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate(Screens.Home.route)
                                }
                            }
                        }
                    }
                }) {
                    Text("Post")
                }

            }
        }
    }



}