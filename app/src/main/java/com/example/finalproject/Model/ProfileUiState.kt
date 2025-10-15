package com.example.finalproject.Model

data class ProfileUiState(
    val photoUrl : String? = null,
    val userName : String = "Guest",
    var name :String = "",
    val message : String? = ""
)
