package com.example.finalproject.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangePasswordScreen(
    onChangePasswordClick: ()-> Unit
){

    var currentPassword by remember{ mutableStateOf("")}
    var newPassword by remember{ mutableStateOf("")}
    var confirmPassword by remember{ mutableStateOf("")}
    val isPasswordMismatch by remember{ derivedStateOf{
       val arePasswordsEntered = newPassword.isNotBlank() && confirmPassword.isNotBlank()

        if(!arePasswordsEntered){
            true
        }else{
            newPassword != confirmPassword
        }
    } }


   Scaffold (
       topBar = { TopBarProfileSettings(
           screenName = "Change password",
           onBackClick = {}
       )}
   ){  innerPadding ->
       Column (
           modifier = Modifier.padding(innerPadding)
               .fillMaxSize()
               .padding(16.dp),
//           horizontalAlignment = Alignment.CenterHorizontally,
       ){

           Spacer(Modifier.height(20.dp))
           //current password
           Text(
               text = "Current Password",
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
                   value = currentPassword,
                   onValueChange = {
//                    viewModel.onNameChanged(it)
                       currentPassword = it
                   },
                   keyboardOptions = KeyboardOptions(
                       keyboardType = KeyboardType.Password
                   ),
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

           //forgot Password

           Text(
               text = "Forgot Password?",
               fontSize = 12.sp,
               color = MaterialTheme.colorScheme.onBackground,
               modifier = Modifier.clickable {
                   //navigate to forgot password screen and ask for email
               }
           )


           //new password

           Spacer(Modifier.height(16.dp))
           Text(
               text = "New Password",
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
                   value = newPassword,
                   onValueChange = {
//                    viewModel.onNameChanged(it)
                       newPassword = it
                   }, keyboardOptions = KeyboardOptions(
                       keyboardType = KeyboardType.Password
                   ),
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

           //confirm password

           Spacer(Modifier.height(16.dp))
           Text(
               text = "Confirm Password",
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
                   value = confirmPassword,
                   onValueChange = {
//                    viewModel.onNameChanged(it)
                       confirmPassword = it
                   },
                   keyboardOptions = KeyboardOptions(
                       keyboardType = KeyboardType.Password
                   ),
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


           if(newPassword != confirmPassword && newPassword.isNotBlank() && confirmPassword.isNotBlank()){
               Spacer(Modifier.height(10.dp))
               Text(
                   text = "The password doesn't match!!",
                   fontSize = 12.sp,
                   color = MaterialTheme.colorScheme.error
               )
           }
           //save password button
           Spacer(Modifier.height(30.dp))

           Button(
               onClick = { onChangePasswordClick()},
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(18.dp)
                   .clip(RoundedCornerShape(45.dp)),
               enabled = !isPasswordMismatch
           ) {
               Text(
                   text = "Change Password",
                   fontSize = 15.sp,
                   modifier = Modifier.padding(5.dp))
           }

       }
   }
}