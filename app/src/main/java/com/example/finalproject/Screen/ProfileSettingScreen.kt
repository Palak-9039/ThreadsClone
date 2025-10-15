package com.example.finalproject.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

    Scaffold(
        topBar = {
            TopBarProfileSettings()
        }
    ) { paddingValues ->

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
                Box(
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    AsyncImage(
                        model = state.photoUrl ?: R.drawable.profile_image,
                        contentDescription = "profile photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { }
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "profile image",
                        modifier = Modifier
                            .clip(CircleShape)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(6.dp)
                            .size(20.dp),
                        tint = Color.White
                    )

                }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Text(
                    "Personal Details",
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(16.dp))

                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 10.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp)),
                    colors = CardDefaults.elevatedCardColors()

                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                        Box(modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)){
                            AsyncImage(
                                model = R.drawable.profile_image,
                                contentDescription = "profile image",
                            )
                        }
                            Spacer(Modifier.width(8.dp))

                            Column (){
                                Text(
                                    text = "Palak Richhariya",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
//                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "@Palak1234",
                                    fontSize = 12.sp,
                                )
                            }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Go to Profile Settings",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )




                    }

                }
                Spacer(Modifier.height(15.dp))

                // notifications and general settings
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 10.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp)),
                    colors = CardDefaults.elevatedCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsOff,
                                contentDescription = "Pause Notification",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Pause Notification",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                            var checked by remember { mutableStateOf(true) }
                            Switch(
                                checked = checked,
                                onCheckedChange = {
                                    checked = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Gray,
                                    checkedTrackColor = Color.Yellow,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.Cyan
                                )
                            )


                    }
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color.Gray.copy(0.2f)
                    )

                    Row(
                        modifier =  Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "General Settings",
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "General Settings",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Go to Language Settings",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

                // Language and appearance
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 10.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp)),
                    colors = CardDefaults.elevatedCardColors()

                ) {
                    //  Dark Mode Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.DarkMode,
                                    contentDescription = "Dark Mode",
                                    modifier = Modifier.size(25.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Dark Mode",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            var isDarkModeEnabled by remember { mutableStateOf(true) }
                            Switch(
                                checked = isDarkModeEnabled,
                                onCheckedChange = { isDarkModeEnabled = it }
                            )

                    }
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color.Gray.copy(0.2f)
                    )

                    // Language Row
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Language",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Go to Language Settings",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        }


                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.2f.dp,
                        color = Color.Gray
                    )

                    //third row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOutline,
                                contentDescription = "My Contact",
                                modifier = Modifier.padding(3.dp).size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "My Contact",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Go to my contacts",
                            modifier = Modifier.padding(end = 5.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                    }
                    }

                Spacer(Modifier.height(15.dp))
                // Terms service and user policy card

                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 10.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp)),
                    colors = CardDefaults.elevatedCardColors()

                ) {
                    //  FAQ Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.QuestionMark,
                                contentDescription = "FAQ",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "FAQ",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Go to Language Settings",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                    }
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color.Gray.copy(0.2f)
                    )

                    // terms of service Row
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "terms of service",
                                modifier = Modifier.size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Terms of Service",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Go to Language Settings",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }


                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.2f.dp,
                        color = Color.Gray
                    )

                    //User Policy row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Policy,
                                contentDescription = "User Policy",
                                modifier = Modifier.padding(3.dp).size(25.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "User Policy",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Go to User Policy",
                            modifier = Modifier.padding(end = 5.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                    }
                }

                }

            }

        }
    }
    }


@Composable
fun TopBarProfileSettings(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        verticalAlignment =Alignment.CenterVertically,
    ){
        IconButton(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                .border(border = BorderStroke(0.1.dp, Color.Gray), shape = CircleShape),
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
            Text(
                text = "Settings",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold
            )

        }
    }
}