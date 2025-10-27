package com.example.finalproject.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.finalproject.R
import com.example.finalproject.ViewModel.ProfileSettingsViewModel

@Composable
fun EditProfileScreen(
//    viewModel : ProfileSettingsViewModel,
//    navController : NavController
) {

//    val state by remember{viewModel.uiState}
    //permission launcher
    //


    //just working on ui for now
    var name by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopBarProfileSettings(
            onBackClick = {/*navController.popBackStack()*/},
            screenName = "Edit Profile"
        ) }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 12.dp),
    ) {

        //edit profile photo option

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            AsyncImage(
                model = /*state.photoUrl ?: */ R.drawable.profile_image,
                contentDescription = "profile photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable {  }// show picture in full scree
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "profile image",
                modifier = Modifier
                    .clip(CircleShape)
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(6.dp)
                    .size(20.dp)
                    /* open gallery to select photo for profile photo */
                    .clickable {  },
                tint = Color.White
            )

        }

        Spacer(Modifier.height(30.dp))
        // edit name text field

        Text(
            text = "Full name",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 10.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
//                    viewModel.onNameChanged(it)
                        name = it
                    },
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.background,
                        unfocusedBorderColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

        }

        Spacer(Modifier.height(16.dp))
        //edit email text field

        Text(
            text = "Email",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 10.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {

            OutlinedTextField(
                value = email,
                onValueChange = {
//                    viewModel.onNameChanged(it)
                    email = it
                },
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.background,
                    unfocusedBorderColor = MaterialTheme.colorScheme.background,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

        }

        Spacer(Modifier.height(16.dp))
        //edit username text field

        Text(
            text = "Username",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 10.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {

            OutlinedTextField(
                value = userName,
                onValueChange = {
//                    viewModel.onNameChanged(it)
                    userName = it
                },
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.background,
                    unfocusedBorderColor = MaterialTheme.colorScheme.background,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

        }
        //current password
        //new password

        Spacer(Modifier.height(25.dp))
        //save changes button


        Button(
            onClick = { /*TODO("Implement save changes from viewmodel")*/},
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clip(RoundedCornerShape(45.dp))
        ) {
            Text(
                text = "Save changes",
                fontSize = 15.sp,
                modifier = Modifier.padding(5.dp))
        }

    }
}
}