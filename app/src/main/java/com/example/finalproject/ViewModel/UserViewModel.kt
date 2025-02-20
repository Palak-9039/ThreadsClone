package com.example.finalproject.ViewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.example.finalproject.NotificationManager
import com.example.finalproject.Util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class UserViewModel : ViewModel() {
    var db = FirebaseDatabase.getInstance()
    var users = db.getReference("users")
    var firestore = Firebase.firestore


    private val _userData = MutableLiveData<User>()
    val userData : LiveData<User> = _userData

    private var _followersList = MutableLiveData<List<String>>()
     var followersList : LiveData<List<String>> = _followersList

    private var _followingList = MutableLiveData<List<String>>()
     var followingList : LiveData<List<String>> = _followingList

    private var _isLoading = MutableLiveData<Boolean>()
    var isLoading : LiveData<Boolean> = _isLoading





    fun fetchUser(userId : String) {
        _isLoading.value = true
        users.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(User::class.java)
                data?.let {
                    _userData.postValue(it!!)
                }
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError : ", "${error.message}")
                _isLoading.value = false
            }

        })
    }




    fun followUser(user_id: String, current_id: String) {
        println("following function mein toh aaya hai")
        _isLoading.postValue(true)

        val followerRef = firestore.collection("Followers").document(user_id)
        val followingRef = firestore.collection("Followings").document(current_id)

        viewModelScope.launch {
            try {
                // Create the document if it does not exist
                followerRef.get().await().let {
                    if (!it.exists()) {
                        followerRef.set(mapOf("followers_id" to listOf<String>())).await()
                    }
                }
                followingRef.get().await().let {
                    if (!it.exists()) {
                        followingRef.set(mapOf("following_id" to listOf<String>())).await()
                    }
                }
                // Update the document
                followerRef.update("followers_id", FieldValue.arrayUnion(current_id)).await()
                followingRef.update("following_id", FieldValue.arrayUnion(user_id)).await()

                _isLoading.postValue(false)

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error following user: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun getFollowers(user_id: String){
        _isLoading.postValue(true)

        firestore.collection("Followers").document(user_id)
            .get()
            .addOnSuccessListener{ value ->
                var list = value?.get("followers_id") as?List<String>?: listOf()
                _followersList.postValue(list)
                _isLoading.postValue(false)
                Log.d("UserViewModel", "Followers fetched successfully: $list")
            }
            .addOnFailureListener {
                Log.e("UserViewModel", "Error getting followers: ${it.message}")

                _isLoading.postValue(false)
            }
    }

    fun getFollowing(user_id: String){
        _isLoading.postValue(true)

        firestore.collection("Followings").document(user_id)
            .get()
            .addOnSuccessListener{value ->
                val list = value?.get("following_id") as?List<String> ?: listOf()
                _followingList.postValue(list)
                _isLoading.postValue(false)
                Log.d("UserViewModel", "Following fetched successfully: $list")
            }
            .addOnFailureListener {
                Log.e("UserViewModel", "Error getting following: ${it.message}")
                _isLoading.postValue(false)
            }
    }




    // for new followers notifications

    fun listenForNewFollowers(userId: String,context: Context) {

        val userRef = firestore.collection("Followers").document(userId)
        var isInitialSnapshot = true

        userRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()){
                Log.d("OneSignal", "Error listening for new followers: ${error?.message}")
                return@addSnapshotListener
            }

            if(isInitialSnapshot){
                Log.d("onesignal","Ignoring initial snapshot for followers")
                isInitialSnapshot = false
                return@addSnapshotListener
            }

            val followers = snapshot.data?.get("followers_id") as? List<String>
            Log.e("onesignal","followers : ${followers}")
            if(followers == null || followers.isEmpty()){
                Log.d("OneSignal", "No followers found for user: $userId")
                return@addSnapshotListener
            }


            val latestFollowerId = followers.lastOrNull()
            if(latestFollowerId == null){
                Log.d("OneSignal", "No latest follower found for user: $userId")
                return@addSnapshotListener
            }

            Log.d("OneSignal", "New follower detected: $latestFollowerId for user: $userId")


           users.child(latestFollowerId).get().addOnSuccessListener {
                val followerName = it.child("userName").getValue(String::class.java)
                Log.e("onesignal", "follower name : $followerName")
                // Get follower's name
                // Get user's OneSignal ID
                users.child(userId).child("oneSignalId")
                    .get()
                    .addOnSuccessListener { dataSnapshot ->
                        val oneSignalId = dataSnapshot.getValue(String::class.java)
                        if (!oneSignalId.isNullOrEmpty()) {
                            NotificationHelper().sendNotification(
                                "New Follower!",
                                "@${followerName} started following you!",
                                oneSignalId
                            )
                        } else {
                            Log.w("OneSignal", "OneSignal ID not found for user: $userId")
                        }
                    }
                    .addOnFailureListener {
                        Log.e("OneSignal", "Failed to get OneSignal ID", it)
                    }
            }.addOnFailureListener{
                Log.d("onesignal","couldn't get followers name")
            }
        }
        }



}