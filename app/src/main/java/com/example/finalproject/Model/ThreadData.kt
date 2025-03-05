package com.example.finalproject.Model
import android.os.Parcelable
//import kotlinx.parcelize.Parcelize


data class ThreadData(
    val uid:String?="",
    val imgUrl:String? = "",
    val thread:String = "",
    val timestamp:String= "",
    var threadId : String? = null,
    var likes : Map<String, Boolean> = emptyMap(),
    var comments : Map<String,CommentData> = emptyMap()
)
