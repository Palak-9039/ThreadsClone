package com.example.finalproject.Screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.example.finalproject.R
import com.example.finalproject.ViewModel.AuthViewModel
import com.example.finalproject.ViewModel.ThreadViewModel
import com.example.finalproject.ViewModel.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun Profile(
    navController: NavController,
    authViewModel: AuthViewModel,
    threadViewModel: ThreadViewModel,
    userViewModel: UserViewModel
){

    val firebaseUser by authViewModel.firebaseUser.observeAsState(null)



    LaunchedEffect(firebaseUser) {
        if (firebaseUser == null) {
            navController.navigate(Screens.SignIn.route) {
                popUpTo(Screens.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val allThreads by threadViewModel.threadData.observeAsState(emptyList())

    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    LaunchedEffect (userId){
        delay(3000)
        Log.d("ProfileScreen", "LaunchedEffect is running")
        if (userId != null) {
            println("user id " + userId)
            threadViewModel.fetchThreads(userId)
        }
    }


    Scaffold(
        bottomBar = { myBottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ProfileHeader(authViewModel,navController)
                LazyColumn(
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(allThreads) { thread ->
                        HorizontalDivider()
                        PostItem(thread)
                        println("all threads : " + allThreads)
                    }
                }
            }
        }
    }


@Composable
fun ProfileHeader(authViewModel: AuthViewModel,
                  navController: NavController){

    val firebaseUser by authViewModel.firebaseUser.observeAsState()
    var context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        AsyncImage(
            model = SharedPref.getImage(context),
            contentDescription = "profile image",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = SharedPref.getName(context), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Android developer", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "500",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = "Followers", fontSize = 14.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "250",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = "Following", fontSize = 14.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authViewModel.SignOut()
                println("firebase user after sign out + "+ firebaseUser)
            }
        ) {
            Text(text = "Logout", color = Color.White, fontSize = 16.sp)
        }
    }
}


@SuppressLint("NewApi")
@Composable
fun PostItem(threadData : ThreadData){
    println("idhar aaya")
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                AsyncImage(
                    model = SharedPref.getImage(LocalContext.current),
                    contentDescription = "Profile image",
                    placeholder = painterResource(R.drawable.profile_image),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Username
                Text(
                    text = SharedPref.getUserName(LocalContext.current),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(text = timestampToReadableTime(threadData.timestamp),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically))
        }

        Column (
            modifier = Modifier.padding(start = 40.dp,end = 10.dp)
        ){

            Text(
                text = threadData.thread,
                fontSize = 15.sp,
                fontWeight = FontWeight(5),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            if(threadData.imgUrl != ""){
                AsyncImage(
                    model = threadData.imgUrl,
                    contentDescription = "Thread image",
                    placeholder = painterResource(R.drawable.profile_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

    }

}