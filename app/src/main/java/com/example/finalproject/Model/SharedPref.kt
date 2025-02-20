package com.example.finalproject.Model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import kotlin.collections.remove

object SharedPref {
    fun storeData(name : String,userName:String,email:String,uid : String,imageUrl : String?,context : Context){
        var pref = context.getSharedPreferences("users",MODE_PRIVATE)
        var editor = pref.edit()

        editor.putString("name",name)
        editor.putString("userName",userName)
        editor.putString("email",email)
        editor.putString("uid",uid)
        editor.putString("imageUrl",imageUrl)
        editor.apply()
    }

    fun saveLastNotifiedTimestamp(context : Context,timestamp : String){
        var pref = context.getSharedPreferences("users", MODE_PRIVATE)
        pref.edit().putString("lastNotifiedThreadTimestamp",timestamp).apply()
    }


    fun getLastNotifiedTimestamp(context : Context) : String {
        var pref = context.getSharedPreferences("users", MODE_PRIVATE)
       return pref.getString("lastNotifiedThreadTimestamp", "") ?: ""
    }

    fun getName(context: Context) : String{
        var pref = context.getSharedPreferences("users",MODE_PRIVATE)
        return pref.getString("name","")!!
    }
    fun getUserName(context: Context) : String{
        var pref = context.getSharedPreferences("users", MODE_PRIVATE)
        return pref.getString("userName","")!!
    }
    fun getEmail(context: Context) : String{
        var pref = context.getSharedPreferences("users", MODE_PRIVATE)
        return pref.getString("email","")!!
    }
    fun getImage(context: Context) : String{
        var pref = context.getSharedPreferences("users", MODE_PRIVATE)
        return pref.getString("imageUrl","")!!
    }



}