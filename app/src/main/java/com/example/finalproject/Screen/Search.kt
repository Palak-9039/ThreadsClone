package com.example.finalproject.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.finalproject.Model.User
import com.example.finalproject.ViewModel.SearchViewModel


@Composable
fun search(navController: NavController,
           searchViewModel: SearchViewModel
){
     Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
    ){
         var search by remember{mutableStateOf("")}
         val users by searchViewModel.userData.observeAsState(emptyList())

         Text(
             text = "Search",
             style = TextStyle(
                 fontWeight = FontWeight.ExtraBold,
                 fontSize = 24.sp
             ),
             modifier = Modifier.padding(top = 16.dp, start = 16.dp)
         )
         OutlinedTextField(
             value = search,
             onValueChange = { search = it },
             label = {
                 Text(
                     "Search User..", style = TextStyle(
                         fontWeight = FontWeight.Light
                     )
                 )
             },
             keyboardOptions = KeyboardOptions(
                 keyboardType = KeyboardType.Text
             ),
             singleLine = true,
             modifier = Modifier.fillMaxWidth(),
             leadingIcon = {
                 Icon(imageVector = Icons.Default.Search, contentDescription = null)
             }
         )

         Scaffold(
             bottomBar = { myBottomBar(navController) }
         ) {
             val filterUser = users.filter{it.name.contains(search,ignoreCase = true)}
             LazyColumn(modifier = Modifier.padding(it)) {
                 items(filterUser){user->
                     userItem(user,navController)
                 }
             }
         }
     }
}


@Composable
fun userItem(user: User, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp).clickable {
                navController.navigate(Screens.OtherProfile.route+"/${user.uid}")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.imageUrl).build(),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(
                text = user.userName,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = user.name,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraLight
                )
            )
        }

    }
}
