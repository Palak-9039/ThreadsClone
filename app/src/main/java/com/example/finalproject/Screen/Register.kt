package com.example.finalproject.Screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.finalproject.ImageUploading.uploadImageToCloudinary
import com.example.finalproject.R
import com.example.finalproject.ViewModel.AuthViewModel

@Composable
fun register(navController: NavController, authViewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var userName by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var imageRef by remember { mutableStateOf<Uri?>(null) }
        var context = LocalContext.current
        val firebaseUser by authViewModel.firebaseUser.observeAsState(null)
        val error by authViewModel.error.observeAsState()

        val isLoading by authViewModel.isLoading.observeAsState(false)



        LaunchedEffect(firebaseUser) {
            if (firebaseUser != null) {
                navController.popBackStack()
                navController.navigate(Screens.Home.route) {
                    popUpTo(Screens.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        LaunchedEffect(key1 = error) {
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }


        val requestToPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }


        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    println("Selected Image URI: $uri") // Debugging log
                    imageRef = uri
                } else {
                    println("No image selected") // Debugging log
                }
            }


        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                println("Permission granted") // Debugging log
                launcher.launch("image/*")
            } else {
                println("Permission denied") // Debugging log
                Toast.makeText(context, "Permission required to select image", Toast.LENGTH_LONG).show()
            }
        }

        if (isLoading) {
            // Show the loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Text(
                "Register",
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(15.dp))

            Image(
                painter = if (imageRef == null) painterResource(id = R.drawable.profile_image)
                else rememberAsyncImagePainter(model = imageRef),
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable {
                        val isGranted = ContextCompat.checkSelfPermission(
                            context,
                            requestToPermission
                        ) == PackageManager.PERMISSION_GRANTED
                        if (isGranted) {
                            launcher.launch("image/*")
                        } else {
                            permissionLauncher.launch(requestToPermission)
                        }
                    },
                contentScale = ContentScale.Crop,
                contentDescription = null
            )

            Spacer(Modifier.height(50.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Enter user name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            Spacer(Modifier.height(15.dp))
            Button(onClick = {
                if (name.isEmpty() || userName.isEmpty() || email.isEmpty() || password.isEmpty() || imageRef == null) {
                    Toast.makeText(context, "Fill all the fields", Toast.LENGTH_LONG).show()
                } else {
                    println("Final Image URI before upload: $imageRef") // Debugging log
                    if (imageRef != null) {
                        uploadImageToCloudinary(
                            context,
                            imageRef!!,
                            "296176452574737",
                            "qQPbMgW7Uih0pGZY4NqHjIqbfiI",
                            "dummy"
                        ) { url ->
                            if (url != null) {
                                println("Image uploaded successfully: $url") // Debugging log
                                authViewModel.register(
                                    email,
                                    password,
                                    name,
                                    userName,
                                    url,
                                    context
                                )
                            } else {
                                println("Image upload failed") // Debugging log
                            }
                        }
                    }
                }

            }) {
                Text("Submit")
            }

            Spacer(Modifier.height(15.dp))
            Text("Already have an account? Login",
                style = TextStyle(
                    fontSize = 15.sp
                ),
                modifier = Modifier.clickable {
                    navController.navigate(Screens.SignIn.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true

                    }

                })
        }

    }
}