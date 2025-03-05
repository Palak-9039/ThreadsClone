package com.example.finalproject.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.ViewModel.ThreadViewModel

@Composable
fun commentScreen(threadId : String,navController: NavController,threadViewModel: ThreadViewModel){



    var text by remember { mutableStateOf("") }
    Scaffold(
        topBar = {MyTopbar()}
    ){
       Column(
           modifier = Modifier
               .fillMaxSize()
               .padding(it),
           verticalArrangement = Arrangement.SpaceBetween,
           horizontalAlignment = Alignment.CenterHorizontally
       ) {
           LazyColumn {

           }
          Row{
              ThreadsTextField(
                  value = text,
                  onValueChange = { text = it },
                  label = "Enter your text here",
                  modifier = Modifier.padding(8.dp),
                  textStyle = TextStyle(
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Normal,
                      color = Color.Black
                  ),
                  cursorColor = Color.Black,
                  labelColor = Color.Gray,
                  clearIconColor = Color.Gray,
                  underlineColor = Color.Black,
                  unfocusedUnderlineColor = Color.LightGray,
                  containerColor = Color.Transparent
              ){
                  threadViewModel.addComment(threadId,text)
                  text = ""
              }
          }
       }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ThreadsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Label",
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    cursorColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.primary,
    clearIconColor: Color = MaterialTheme.colorScheme.primary,
    underlineColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedUnderlineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    containerColor: Color = Color.Transparent,
    onSendButtonClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            maxLines = maxLines,
            label = {
                Text(
                    text = label,
                    color = labelColor
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onSendButtonClick() }) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Clear",
                            tint = clearIconColor
                        )
                    }
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = underlineColor,
                unfocusedBorderColor = unfocusedUnderlineColor,
                cursorColor = cursorColor,
                containerColor = containerColor
            )
        )
    }
}

@Composable
fun MyTopbar(){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton({}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
            Spacer(Modifier.width(5.dp))
            Text(
                text = "Thread",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

        }
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton({}) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null
                )
            }
            Spacer(Modifier.width(5.dp))
            IconButton({}) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null
                )
            }
        }
    }
}