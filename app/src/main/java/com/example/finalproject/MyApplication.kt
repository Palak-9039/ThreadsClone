package com.example.finalproject

import android.app.Application
import com.onesignal.OneSignal

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        OneSignal.initWithContext(this)
        OneSignal.setAppId("f63b86c5-27be-4584-bc86-08255699eb18")    }
}