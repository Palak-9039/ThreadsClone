package com.example.finalproject.Screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.finalproject.ViewModel.AuthViewModel

@Composable
fun login(navController: NavController,authViewModel: AuthViewModel){

    val isLoading by authViewModel.isLoading.observeAsState(false)

    Column (
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        var email by remember{ mutableStateOf("") }
        var password by remember{ mutableStateOf("") }

        val firebaseUser by authViewModel.firebaseUser.observeAsState(null)
        var context = LocalContext.current
        val error by authViewModel.error.observeAsState()


        LaunchedEffect(firebaseUser) {
            if (firebaseUser != null) {
                navController.navigate(Screens.Home.route) {
                    navController.popBackStack()
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



        if (isLoading) {
            // Show the loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Text(
                "Login",
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(50.dp))

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

            Spacer(Modifier.height(20.dp))

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

            Button(onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "fill the details", Toast.LENGTH_LONG).show()
                } else {
                    authViewModel.login(email, password, context)
                    email = ""
                    password = ""
                }
            }) {
                Text("Submit")
            }

            Spacer(Modifier.height(15.dp))
            Text("Don't have an account? Register",
                style = TextStyle(
                    fontSize = 15.sp
                ),
                modifier = Modifier.clickable {
                    navController.navigate(Screens.SignUp.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }

                })
        }
    }
}