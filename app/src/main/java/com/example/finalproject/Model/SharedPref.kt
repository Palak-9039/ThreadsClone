package com.example.finalproject.Model

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.compose.foundation.layout.add
import kotlin.collections.remove

object SharedPref {
    private const val PREF_NAME = "users"
    private const val ACKNOWLEDGED_NOTIFICATIONS_KEY = "acknowledgedNotifications"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    }

    fun storeData(name : String,userName:String,email:String,uid : String,imageUrl : String?,context : Context){
        val pref = context.getSharedPreferences("users",MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString("name",name)
        editor.putString("userName",userName)
        editor.putString("email",email)
        editor.putString("uid",uid)
        editor.putString("imageUrl",imageUrl)
        editor.apply()
    }

    fun saveName(context: Context,name : String){
        val pref = context.getSharedPreferences("users", MODE_PRIVATE)
        pref.edit().putString("name",name).apply()
    }

    fun saveImage(context: Context, imageUrl: String){
        val pref = context.getSharedPreferences("users",MODE_PRIVATE)
        pref.edit().putString("imageUrl",imageUrl).apply()
    }

    fun saveUsername(context: Context, username: String){
        val pref = context.getSharedPreferences("users",MODE_PRIVATE)
        pref.edit().putString("userName",username).apply()
    }



    fun markNotificationAcknowledged(context: Context, threadId: String) {
        val acknowledgedNotifications = getAcknowledgedNotifications(context).toMutableSet()
        acknowledgedNotifications.add(threadId)
        getSharedPreferences(context).edit()
            .putStringSet(ACKNOWLEDGED_NOTIFICATIONS_KEY, acknowledgedNotifications)
            .apply()
    }

    fun isNotificationAcknowledged(context: Context, threadId: String): Boolean {
        return getAcknowledgedNotifications(context).contains(threadId)
    }

    private fun getAcknowledgedNotifications(context: Context): Set<String> {
        return getSharedPreferences(context).getStringSet(ACKNOWLEDGED_NOTIFICATIONS_KEY, emptySet()) ?: emptySet()
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