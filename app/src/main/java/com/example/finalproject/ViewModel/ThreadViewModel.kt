package com.example.finalproject.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.NotificationManager
import com.example.finalproject.Util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ThreadViewModel : ViewModel() {
    var db = FirebaseDatabase.getInstance()
    var threadRef = db.getReference("threads")
    var users = db.getReference("users")
    var firestore = Firebase.firestore
    private val notificationHelper = NotificationHelper()

    private var _firebaseUser = MutableLiveData<FirebaseUser?>()
    var firebaseUser: LiveData<FirebaseUser?> = _firebaseUser

    private var _isPosted = MutableLiveData<Boolean>()
    var isPosted: LiveData<Boolean> = _isPosted

    private var _isLoading = MutableLiveData<Boolean>()
    var isLoading: LiveData<Boolean> = _isLoading

    private var _threadData = MutableLiveData<List<ThreadData>>()
    var threadData : LiveData<List<ThreadData>> = _threadData

    private var threadListener: ChildEventListener? = null
    private var isListening = false
  hello world

    fun saveThread(
        uid: String?,
        imageUrl: String? = "",
        thread: String,
        onSuccess: (Boolean) -> Unit
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val threadObject =
                ThreadData(uid, imageUrl, thread, System.currentTimeMillis().toString())

            threadRef.child(threadRef.push().key!!).setValue(threadObject)
                .addOnSuccessListener {
                    onSuccess(true)
                    _isPosted.value = true
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    onSuccess(false)
                    _isPosted.value = false
                    _isLoading.value = false
                }
        }
    }


    fun fetchThreads(uid: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                threadRef.orderByChild("uid").equalTo(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val resultList = snapshot.children.reversed().mapNotNull {
                                it.getValue(ThreadData::class.java)
                            }
                            _threadData.postValue(resultList)
                            _isLoading.postValue(false)
                        }


                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Error fetching threads: ${error.message}")
                            _isLoading.postValue(false)

                        }
                    })
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching threads: ${e.message}")
                _isLoading.postValue(false)

            }
        }
    }


    @SuppressLint("NewApi")
    fun listenForNewThreads(context : Context) {

        if (isListening) return
        Log.d("onesignal","listenForNewFollowers mein aaya")

        threadListener?.let { threadRef.removeEventListener(it) } // Remove any existing listener


        var lastNotifiedTimestamp = SharedPref.getLastNotifiedTimestamp(context)

        if(lastNotifiedTimestamp.isEmpty() || lastNotifiedTimestamp.equals("")){
            Log.d("onesignal","last timestamp : ${lastNotifiedTimestamp}")

            val currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            lastNotifiedTimestamp = currentDateTime.format(formatter)
            SharedPref.saveLastNotifiedTimestamp(context ,lastNotifiedTimestamp)
            Log.d("onesignal","save ke baad ${SharedPref.getLastNotifiedTimestamp(context)}")

            Log.d("OneSignal", "First run detected. Setting initial timestamp.")
        }

        Log.d("OneSignal", "Started listening for new threads")  // Debug log

        threadListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Access threadData inside threadId
                val threadData = snapshot.getValue(ThreadData::class.java) ?: return
                val threadTimestamp = threadData.timestamp

                val threadTitle = threadData.thread
                val userId = threadData.uid ?: return


                if (threadTimestamp <= lastNotifiedTimestamp) {
                    return
                }

                Log.d("onesignal", "else mein aa gaye")
                lastNotifiedTimestamp = threadTimestamp
                SharedPref.saveLastNotifiedTimestamp(context, lastNotifiedTimestamp)


                viewModelScope.launch {
                    try {
                        // Fetch followers from Firestore
                        firestore.collection("Followers").document(userId).get()
                            .addOnSuccessListener { followersDoc ->
                                if (!followersDoc.exists()) {
                                    Log.d("oneSignal", "folllowers esixt nahi karta")
                                    return@addOnSuccessListener
                                }

                                Log.d("oneSignal", "user id : $userId")
                                val followers =
                                    followersDoc?.get("followers_id") as? List<String> ?: listOf()
                                Log.d("onesignal", "list of followers : ${followers}")


                                followers.forEach { followerId ->
                                    Log.d("OneSignal", "follower id : ${followerId}")


                                    // Get OneSignal ID of the follower from Realtime Database
                                    db.getReference ("users").child(followerId).child("oneSignalId").get()
                                        .addOnSuccessListener { oneSignalSnapshot ->
                                            val oneSignalId =
                                                oneSignalSnapshot.getValue(String::class.java)
                                            Log.d("OneSignal", "onesignal id : ${oneSignalId}")
                                            if (!oneSignalId.isNullOrEmpty()) {
                                                Log.d(
                                                    "OneSignal",
                                                    "Sending notification to: $followerId ($oneSignalId)"
                                                ) // Debug log

                                                sendNotificationFromthreads(
                                                    title = "New Thread Posted!",
                                                    message = "Check out the latest thread by : ${threadTitle}",
                                                    playerId = oneSignalId
                                                )
                                            }
                                        }
                                }
                            }.addOnFailureListener {
                                Log.d("OneSignal", "Failed to fetch followers", it) // Debug log

                            }

                    }catch (e:Exception){
                        Log.e("OneSignal", "Failed to fetch followers or send notification", e)
                    }
                }
//            threads.orderByChild("timestamp").addChildEventListener(threadListener!!)
            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.d("OneSignal", "data base errror : ${error.message}") // Debug log

            }
        }
        threadRef.orderByChild("timestamp").addChildEventListener(threadListener!!)
        isListening = true
    }

    fun sendNotificationFromthreads(title: String, message: String, playerId: String) {
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



    override fun onCleared() {
        super.onCleared()
        // Remove the listener when the ViewModel is cleared
        if (threadListener != null) {
        threadRef.removeEventListener(threadListener!!)
        isListening = false
        }
    }
}
