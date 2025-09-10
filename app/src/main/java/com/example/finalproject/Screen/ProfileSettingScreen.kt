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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.finalproject.ImageUploading.uploadImageToCloudinary
import com.example.finalproject.R
import com.example.finalproject.ViewModel.ProfileSettingsViewModel
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import com.example.finalproject.Repository.UserRepository


@SuppressLint("InlinedApi")
@Composable
fun ProfileSettingScreen(){
    val context = LocalContext.current

    val viewModel : ProfileSettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = UserRepository(context.applicationContext)
                return ProfileSettingsViewModel(repo) as T
            }
        }
    )

    val state by viewModel.uiState
    var imageRef by remember{ mutableStateOf<Uri?>(null)}

    // are in the state now
//    var photoUrl by remember { mutableStateOf<String?>(SharedPref.getImage(context)) }
//    var userName by remember{ mutableStateOf<String?>(SharedPref.getName(context))}
//    var name by remember { mutableStateOf<String>(SharedPref.getName(context)) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }



    LaunchedEffect(state.message) {
        state.message?.let {message ->
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }




    //Permission Handling

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



    // UI Layout

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {


        //change profile photo
        AsyncImage(
            model = state.photoUrl?: R.drawable.profile_image,
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
                       viewModel.updatePhoto(imageRef)
                }
            } 
        ) {
            Text(
                text = "change profile photo"
            )
        }


        //update username
        OutlinedTextField(
            value = state.userName ?:"",
            onValueChange = {viewModel.onUserNameChanged(it)},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            colors = TextFieldDefaults.colors(
               focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Button(
            onClick = {
                viewModel.updateUserName(state.userName)
            }
        ) {
            Text(
                text = "change username"
            )
        }


        //Update name
        OutlinedTextField(
            value = state.name ?:"",
            onValueChange = {viewModel.onNameChanged(it)},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Button(
            onClick = {
               viewModel.updateName(state.name)
            }
        ) {
            Text(
                text = "change name"
            )
        }
        //update password
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(10.dp),
            value = currentPassword,
            onValueChange = {currentPassword = it},
            placeholder = {Text("Enter current password")},
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(10.dp),
            value = newPassword,
            onValueChange = {newPassword = it},
            placeholder = {Text("Enter new password")},
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            )
        )


        Button(
            onClick = {
                viewModel.updatePassword(currentPassword,newPassword)
            }
        ) {
            Text(
                text = "Change password",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}