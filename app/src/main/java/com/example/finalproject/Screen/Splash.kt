package com.example.finalproject.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.MyApplication
import com.example.finalproject.R
import com.example.finalproject.ViewModel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun splash(navController: NavController,
           authViewModel: AuthViewModel){
    val firebasUser by authViewModel.firebaseUser.observeAsState(null)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){


            Icon(painter = painterResource(R.drawable.threads_logo), contentDescription = "thread logo",
                modifier = Modifier.size(100.dp))

        LaunchedEffect(firebasUser) {
            delay(1500)

            if(firebasUser != null){
//                authViewModel.saveOneSignalPlayerId().join()
//                authViewModel.saveOneSignalIdToDatabase()
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Splash.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
            }else{
                navController.navigate(Screens.SignIn.route){
                    popUpTo(Screens.Splash.route){inclusive = true}
                }
            }
        }
    }
}