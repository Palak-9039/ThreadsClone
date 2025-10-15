package com.example.finalproject.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Counter(){
    val viewmodel = viewModel<counterviewmodel>()

    var number = viewmodel.counterFlow.collectAsState(10)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Text(
            text = number.value.toString(),
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}