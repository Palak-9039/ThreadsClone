package com.example.finalproject.Model

data class ThreadData(
    val uid:String?="",
    val imgUrl:String? = "",
    val thread:String = "",
    val timestamp:String= "",
    var threadId : String? = null,
    var likes : Map<String, Boolean> = emptyMap()
)
