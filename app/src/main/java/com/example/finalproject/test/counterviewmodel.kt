package com.example.finalproject.test

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class counterviewmodel : ViewModel() {

    var startValue = 10

    var counterFlow = flow<Int> {
        while (startValue > 0){
            delay(1000L)
            startValue--
            emit(startValue)
        }
    }
}