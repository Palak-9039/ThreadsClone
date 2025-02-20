package com.example.finalproject.Model

data class User(
    var name : String = "",
    val userName : String = "",
    val email:String = "",
    val password : String= "",
    val uid : String? = null,
    val imageUrl : String? = "",
    val oneSignalId : String? = null
)
