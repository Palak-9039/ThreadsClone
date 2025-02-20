package com.example.finalproject.Util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class NotificationHelper {
    fun sendNotification(title: String, message: String, playerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("app_id", "f63b86c5-27be-4584-bc86-08255699eb18") // Your OneSignal App ID
                    put("include_player_ids", JSONArray().put(playerId)) // Target user
                    put("headings", JSONObject().put("en", title)) // Notification title
                    put("contents", JSONObject().put("en", message)) // Notification message
                }

                val url = URL("https://onesignal.com/api/v1/notifications")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty("Authorization", "Basic os_v2_app_6y5ynrjhxzcyjpegbasvngplddweea2lgcvu5qfz5fycprecyqcegglyjemxalv7z3pxvjpkrrxvp6mxzifijemxisof5jlqk2lj3oq") // Replace with your REST API key

                // Send JSON data
                DataOutputStream(connection.outputStream).use { it.writeBytes(json.toString()) }

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode in 200..299) {
                    Log.d("OneSignal", "Notification sent successfully! Response: $responseMessage")
                } else {
                    Log.e("OneSignal", "Failed to send notification. Response Code: $responseCode, Message: $responseMessage")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("OneSignal", "Error sending notification", e)
            }
        }
    }

}