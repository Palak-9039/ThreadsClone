package com.example.finalproject.Screen

import android.service.autofill.UserData
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.finalproject.Model.CommentData
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.example.finalproject.R
import com.example.finalproject.ViewModel.ThreadViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Composable
fun commentScreen(threadId : String,navController: NavController,threadViewModel: ThreadViewModel){

//    val comments by threadViewModel.comments.observeAsState(emptyList())

    val comments = threadViewModel.comments
    val threadDatas by threadViewModel.threadData.observeAsState()

    LaunchedEffect(threadId,comments){
        threadViewModel.fetchComments(threadId)
    }


    var threadData by remember { mutableStateOf<ThreadData?>(null) }
    var userDataMap by remember { mutableStateOf<Map<String,User>>(emptyMap()) }
    var coroutineScope = rememberCoroutineScope()

    LaunchedEffect (threadId){
        coroutineScope.launch {
            threadViewModel.getThreadData(threadId){
                threadData = it
                Log.d("CommentsScreen","thread data $threadData")
            }
        }
    }




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

           val commentsForThread = comments[threadId]
           if(commentsForThread.isNullOrEmpty()){
               Text("No comments yet",
                   fontSize = 40.sp,
                   color = MaterialTheme.colorScheme.onBackground)
           }else{
               if (threadData != null) {
                   LazyColumn {
                       items(commentsForThread) { comment ->
                           HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary)
                           CommentItem(comment, threadData!!)
                       }
                   }
               }else {
                   // Show a loading indicator while threadData is being fetched
                   CircularProgressIndicator()
                   Text("Loading thread data...")
               }
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
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                  ),
                  cursorColor = Color.Black,
                  labelColor = Color.Gray,
                  clearIconColor = Color.Gray,
                  underlineColor = MaterialTheme.colorScheme.onSecondary,
                  unfocusedUnderlineColor = MaterialTheme.colorScheme.onSecondary,
                  containerColor = Color.Transparent
              ){
                  threadViewModel.addComment(threadId,text)
                  text = ""
              }
          }
       }
    }
}

@Composable
fun CommentItem(comment: CommentData,threadData: ThreadData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ){
//        AsyncImage(
//            model = userData.imageUrl,
//            contentDescription = null,
//            placeholder = painterResource(R.drawable.profile_image),
//            modifier = Modifier
//                .size(40.dp)
//                .clip(CircleShape),
//            contentScale = ContentScale.FillBounds
//        )
        Spacer(Modifier.height(8.dp))
        Column {
            Text(text = comment.userName ?: "Unknown User",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            Text(text = comment.comment ?: "No comment",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(containerColor)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
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