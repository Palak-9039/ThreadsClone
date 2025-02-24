package com.example.finalproject.Screen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.example.finalproject.ViewModel.HomeViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.finalproject.R
import com.example.finalproject.ViewModel.ThreadViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@RequiresApi(Build.VERSION_CODES.O)

 fun timestampToReadableTimeFollowing(timestamp: String):String{
    val instant = Instant.ofEpochMilli(timestamp.toLong())
    var formatter = DateTimeFormatter.ofPattern("hh:mm a, dd-MMM-y")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)

}

@Composable
fun followingThreadsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    threadViewModel : ThreadViewModel){


    val isLoading by homeViewModel.isLoading.observeAsState()
    val threadAndUser by homeViewModel.threadAndUser.observeAsState(emptyList())

    var isRefreshing by remember{ mutableStateOf(false)}
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    LaunchedEffect(isRefreshing){
        Log.d("SwipeRefresh", "isRefreshing: $isRefreshing")
        Log.d("SwipeRefresh", "swipeRefreshState.isRefreshing: ${swipeRefreshState.isRefreshing}")
        if(isRefreshing){
            homeViewModel.refreshData()
            isRefreshing = false
        }
    }
    Column{
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                Log.d("SwipeRefresh", "onRefresh called")
                isRefreshing = true
            },
            modifier = Modifier
                .fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isLoading == true) {
                // Display the progress indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        AsyncImage(
                            model = SharedPref.getImage(LocalContext.current),
                            contentDescription = "Profile image",
                            placeholder = painterResource(R.drawable.profile_image),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.FillBounds
                        )
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                text = SharedPref.getName(LocalContext.current),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "What's new?",
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 10.dp)
                    ) {
                        items(threadAndUser) { pair ->
                            HorizontalDivider()
                            threadItem(pair.first, pair.second, navController,threadViewModel)
                        }
                    }
                }
            }
        }
     }
    }
}

@SuppressLint("NewApi")
@Composable
fun threadItemFollowingThreads(threadData : ThreadData,user: User,navController: NavController){

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
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.imageUrl).build(),
                        contentDescription = "Profile image",
                        placeholder = painterResource(R.drawable.profile_image),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate(Screens.OtherProfile.route+"/${user.uid}")

                            },
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Username
                    Text(
                        text = user.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(text = threadData.timestamp,
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