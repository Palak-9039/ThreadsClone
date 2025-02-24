package com.example.finalproject.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.finalproject.R
import com.example.finalproject.ViewModel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun splash(navController: NavController,
           authViewModel: AuthViewModel){
    val firebaseUser by authViewModel.firebaseUser.observeAsState(null)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){


            Icon(painter = painterResource(R.drawable.threads_logo), contentDescription = "thread logo",
                modifier = Modifier.size(100.dp))

        LaunchedEffect(firebaseUser) {
            delay(1500)

            if(firebaseUser != null){
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