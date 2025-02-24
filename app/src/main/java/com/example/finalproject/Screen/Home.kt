package com.example.finalproject.Screen

//import androidx.compose.ui.text.intl.Locale
//import kotlin.time.Duration

//import java.time.ZoneOffset
//import java.time.format.DateTimeFormatter
import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.finalproject.ViewModel.HomeViewModel
import com.example.finalproject.ViewModel.ThreadViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


//@RequiresApi(Build.VERSION_CODES.O)

@SuppressLint("NewApi")
fun timestampToReadableTime(timestamp: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(timestamp, formatter)
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val duration = Duration.between(dateTime, now)

        when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            else -> dateTime.format(
                DateTimeFormatter.ofPattern(
                    "MMM dd",
                    Locale.getDefault()
                )
            )
        }
    } catch (e: Exception) {
        // Handle parsing errors gracefully
        e.printStackTrace()
        "Invalid Date"
    }
}

@Composable
fun Home(
    navController: NavController,
    homeViewModel: HomeViewModel,
    threadViewModel: ThreadViewModel
) {


    val isLoading by homeViewModel.isLoading.observeAsState()
    val threadAndUser by homeViewModel.threadAndUser.observeAsState(emptyList())

    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    LaunchedEffect(isRefreshing) {
        Log.d("SwipeRefresh", "isRefreshing: $isRefreshing")
        Log.d("SwipeRefresh", "swipeRefreshState.isRefreshing: ${swipeRefreshState.isRefreshing}")
        if (isRefreshing) {
            homeViewModel.refreshData()
            isRefreshing = false
        }
    }
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                Log.d("SwipeRefresh", "onRefresh called")
                isRefreshing = true
            },
            modifier = Modifier
                .fillMaxSize()
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
                                threadItem(pair.first, pair.second, navController, threadViewModel)
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
fun threadItem(
    threadData: ThreadData, user: User,
    navController: NavController,
    threadViewModel: ThreadViewModel
) {

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(0) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    LaunchedEffect(threadData) {
        isLiked = currentUserId?.let { threadData.likes[it] == true } ?: false
        likeCount = threadData.likes.count { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.imageUrl).build(),
                    contentDescription = "Profile image",
                    placeholder = painterResource(R.drawable.profile_image),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate(Screens.OtherProfile.route + "/${user.uid}")

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
            Text(
                text = timestampToReadableTime(threadData.timestamp),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            )
        }

        Column(
            modifier = Modifier.padding(start = 40.dp, end = 10.dp)
        ) {

            Text(
                text = threadData.thread,
                fontSize = 15.sp,
                fontWeight = FontWeight(5),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (threadData.imgUrl != "") {
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

        Row(
            modifier = Modifier.padding(top = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            LikeButton(isLiked = isLiked,
                likeCount = likeCount,
                onLikeClicked = {
                    println("udi is ${threadData.threadId}")
                    threadViewModel.toggleLike(threadData.threadId!!)
                })
//                Spacer(Modifier.width(4.dp))
//                Text(text = "100", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(4.dp))

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.ModeComment,
                    contentDescription = "comment"
                )
            }

        }
    }

}

@Composable
fun LikeButton(
    isLiked: Boolean,
    likeCount: Int,
    onLikeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onLikeClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLiked) Color.Red else Color.Gray,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(text = likeCount.toString())
    }
}