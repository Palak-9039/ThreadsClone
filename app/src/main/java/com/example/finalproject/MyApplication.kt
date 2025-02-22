package com.example.finalproject

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.onesignal.OneSignal


class MyApplication : Application() {

    private val ONESIGNAL_APP_ID = "f63b86c5-27be-4584-bc86-08255699eb18"
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate() {
        super.onCreate()

        // Enable verbose OneSignal logging to debug issues
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        Log.d("OneSignal", "MyApplication onCreate: OneSignal initialized.")


        sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        // Set a listener for authentication state changes

        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch",true)

        if(isFirstLaunch){
            clearAppData()
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply()
        }

    }

    private fun clearAppData() {
        // Clear SharedPreferences
//        sharedPreferences.edit().clear().apply()

        // Clear other data (e.g., internal storage files) if needed
        // ...
    }
}