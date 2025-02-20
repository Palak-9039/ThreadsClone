package com.example.finalproject

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class NotificationManager(private val context : Context) {
    private val CHANNEL_ID = "threadsNotification"


    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Threads Channel"
            val desriptionText = "Threads Channel Description"

            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = desriptionText
            }

            val notificationManager : NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, title : String, message: String){
        val intent = Intent(context,MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(context,0,intent,
            PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context,CHANNEL_ID)
            .setSmallIcon(R.drawable.threads_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)){
            val notificationId = System.currentTimeMillis().toInt()

            notify(notificationId,builder.build())
        }
    }

    fun notifyFollowersOfNewThread(userId : String){
        val firestore = Firebase.firestore
        firestore.collection("Followers").document(userId).get()
            .addOnSuccessListener {document ->
                if(document != null && document.exists()){
                    val followers = document.get("followers_id") as? List<String> ?: emptyList()

                    for(follower in followers){
                        showNotification(context,"New Thread!",
                            "A user you follow has posted a new thread!")
                    }
                }
            }
    }
}