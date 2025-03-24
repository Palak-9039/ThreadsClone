package com.example.finalproject.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.Model.CommentData
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.example.finalproject.Util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.w3c.dom.Comment
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class ThreadViewModel : ViewModel() {
    var db = FirebaseDatabase.getInstance()
    var threadRef = db.getReference("threads")
    var userRef = db.getReference("users")
    var firestore = Firebase.firestore
    private val notificationHelper = NotificationHelper()

    private var _firebaseUser = MutableLiveData<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    var firebaseUser: LiveData<FirebaseUser?> = _firebaseUser

    private var _isPosted = MutableLiveData<Boolean>()
    var isPosted: LiveData<Boolean> = _isPosted

    private var _isLoading = MutableLiveData<Boolean>()
    var isLoading: LiveData<Boolean> = _isLoading

    private var _threadData = MutableLiveData<List<ThreadData>>()
    var threadData: LiveData<List<ThreadData>> = _threadData


    private var threadListener: ChildEventListener? = null
    private var isListening = false

    private var initialThreadsLoaded = false // Add this flag
    private var lastProcessedTimestamp: String? = null // Add this variable

//    private val _comments = MutableLiveData<List<CommentData>>()
//    var comments : LiveData<List<CommentData>> = _comments

    private val _comments = mutableStateMapOf<String, List<CommentData>>()
    val comments: Map<String, List<CommentData>> = _comments

    private var currentThreadId: String? = null



    fun getThreadData(threadId : String,onResult: (ThreadData?) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            try {
                   val snapshot =  threadRef.child(threadId).get().await()
                    val threadData = snapshot.getValue(ThreadData::class.java)

                    onResult(threadData)
            }catch (e : Exception){
                Log.e("ThreadViewModel", "Error getting thread data: ${e.message}")
                onResult(null)
            }
        }
    }

    fun getUser(userId : String?,onResult: (User?) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            if (userId == null) {
                Log.e("ThreadViewModel", "getUser called with null userId")
                onResult(null)
                return@launch // Exit the coroutine early
            }
            try {
                val snapshot = userRef.child(userId).get().await()
                val user = snapshot.getValue(User::class.java)
                onResult(user)
            } catch (e: Exception) {
                Log.e("ThreadViewModel", "Error getting user data: ${e.message}")
                onResult(null)
            }
        }
    }

    //Comment feature related functions

    fun addComment(threadId: String, commentText : String){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                var currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                var currentUserSnapshot = userRef.child(currentUserId!!).get().await()

                var currentUserName = currentUserSnapshot.getValue(User::class.java)?.userName ?: "Unknown"

                val commentId = threadRef.child(threadId).child("comments")
                    .push().key
                val commentData = CommentData(
                    commentId = commentId,
                    userId =currentUserId,
                    userName = currentUserName,
                    comment = commentText,
                    timestamp = System.currentTimeMillis()
                )

                threadRef.child(threadId).child("comments").child(commentId!!)
                    .setValue(commentData).await()

                sendCommentNotification(threadId,commentData)

            }catch (e:Exception){
                Log.e("Comments","Error in adding comment : ${e.message}")
            }
        }

    }

    private var commentListener: ValueEventListener? = null


    fun fetchComments(threadId : String){
        commentListener?.let {
            threadRef.child(threadId).child("comments").removeEventListener(it)
        }

//        _comments.value = emptyList()
        currentThreadId = threadId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val commentRef = threadRef.child(threadId).child("comments")

                commentListener = commentRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fetchedComments  = mutableListOf<CommentData>()
                        for (commentSnapshot in snapshot.children){
                            val comment = commentSnapshot.getValue(CommentData::class.java)
                            comment?.let { fetchedComments.add(comment) }
                        }
                        _comments[threadId] = fetchedComments.sortedBy { it.timestamp }
                        Log.d("CommentsScreen","comments are : ${comments[threadId]}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ThreadViewModel", "Error fetching comments: ${error.message}")
                        _comments[threadId] = emptyList()
                    }

                })
            }catch (e : Exception){
                Log.e("ThreadViewModel", "Error fetching comments: ${e.message}")
               _comments[threadId] = emptyList()
            }
        }
    }

    fun sendCommentNotification(threadId: String,commentData: CommentData){
        viewModelScope.launch (Dispatchers.IO){
            try {
                val threadSnapshot = threadRef.child(threadId).get().await()
                val threadData = threadSnapshot.getValue(ThreadData::class.java)?:return@launch

                val threadOwnerId = threadData.uid

                if(commentData.userId == threadOwnerId){
                    Log.d("Notification", "User commented on their own thread, no notification sent.")
                    return@launch
                }

                val commenterUsername = commentData.userName ?: "Someone"

                val threadOwnerUserSnapshot = userRef.child(threadOwnerId!!).get().await()
                val threadOwnerOnesignalId = threadOwnerUserSnapshot.getValue(User::class.java)?.oneSignalId

                if(threadOwnerOnesignalId != null){
                    notificationHelper.sendNotification(
                        title = "New Comment",
                        message = "$commenterUsername commented on your thread!",
                        playerId = threadOwnerOnesignalId
                    )
                }else{
                    Log.e("Notification", "Thread owner's OneSignal ID not found.")
                }


            }catch (e : Exception){
                Log.e("Comments", "Error sending notification: ${e.message}")
            }
        }
    }





    // Likes feature related functions
    fun toggleLike(threadId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val threadRef = threadRef.child(threadId)
                val threadSnapshot = threadRef.get().await()
                if (!threadSnapshot.exists()) {
                    Log.e("toggleLike", "Thread does not exist in Firebase: $threadId")
                    return@launch
                }

                val threadData = threadSnapshot.getValue(ThreadData::class.java)
                println("threadData is : ${threadData}")
                threadData?.let {
                    val currentLikes = it.likes.toMutableMap()
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    println("userid is : $currentUserId")

                    if (currentUserId != null) {
                        if (currentLikes.containsKey(currentUserId)) {
                            currentLikes.remove(currentUserId)
                        } else {
                            currentLikes[currentUserId] = true
                        }
                        threadRef.child("likes").setValue(currentLikes).await()

                        sendLikeNotification(threadData,currentUserId)
                    }

                }
            } catch (e: Exception) {
                Log.e("ThreadViewModel", "Error toggling like: ${e.message}")
            }
        }
    }


    private fun sendLikeNotification(threadData: ThreadData, likerUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val threadOwnerId = threadData.uid

                // Don't send a notification if the user likes their own thread
                if (likerUserId == threadOwnerId) {
                    Log.d("Notification", "User liked their own thread, no notification sent.")
                    return@launch
                }

                // Get the liker's username
                val likerUserSnapshot = userRef.child(likerUserId).get().await()
                val likerUsername = likerUserSnapshot.getValue(User::class.java)?.userName ?: "Someone"

                // Get the thread owner's OneSignal Player ID
                val threadOwnerUserSnapshot = userRef.child(threadOwnerId!!).get().await()
                val threadOwnerPlayerId = threadOwnerUserSnapshot.getValue(User::class.java)?.oneSignalId

                if (threadOwnerPlayerId != null) {
                    // Send the notification
                    notificationHelper.sendNotification(
                        title = "Liked thread",
                        message = "$likerUsername liked your thread",
                        threadOwnerPlayerId
                    )
                } else {
                    Log.e("Notification", "Thread owner's OneSignal ID not found.")
                }
            } catch (e: Exception) {
                Log.e("Notification", "Error sending notification: ${e.message}")
            }
        }
    }



    // Adding a new thread
    @SuppressLint("NewApi")
    fun saveThread(
        uid: String?,
        imageUrl: String? = "",
        thread: String,
        onSuccess: (Boolean) -> Unit
    ) {
        Log.e("threadviewmodel", "save thread mein aaya")
        _isLoading.value = true
        viewModelScope.launch {
            val currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
            // Format the timestamp as an ISO 8601 string
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val threadTimestamp = currentDateTime.format(formatter)


            val threadId = threadRef.push().key!!

            val threadObject =
                ThreadData(
                    uid, imageUrl, thread, threadTimestamp,
                    likes = emptyMap(),
                    threadId = threadId,
                    comments = emptyMap()
                )

            threadRef.child(threadId).setValue(threadObject)
                .addOnSuccessListener {
                    threadObject.threadId = threadId
                    onSuccess(true)
                    _isPosted.value = true
                    _isLoading.value = false
                    Log.e("threadviewmodel", "save ho gaya thread")

                }
                .addOnFailureListener {
                    onSuccess(false)
                    _isPosted.value = false
                    _isLoading.value = false
                    Log.e("threadviewmodel", "save thread nhi ho paya")
                }
        }
    }

 // Fetching threads
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
                            Log.e("threadviewmodel", "threads fetch ho gyi")
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

// Listening for new threads for notifications
    @SuppressLint("NewApi")
    fun listenForNewThreads(context: Context) {

        if (isListening) {
            Log.e("threadviewmodel", "listenForNewFollowers se wapas gaya")
//            return
        }


        Log.e("threadviewmodel", "listenForNewthreads mein aaya")

        threadListener?.let {
            threadRef.removeEventListener(it)
            isListening = false
        } // Remove any existing listener


        var lastNotifiedTimestamp = SharedPref.getLastNotifiedTimestamp(context)

        if (lastNotifiedTimestamp.isEmpty() || lastNotifiedTimestamp.equals("")) {
            Log.d("threadviewmodel", "last timestamp : ${lastNotifiedTimestamp}")

            val currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            lastNotifiedTimestamp = currentDateTime.format(formatter)
            SharedPref.saveLastNotifiedTimestamp(context, lastNotifiedTimestamp)
            Log.d("threadviewmodel", "save ke baad ${SharedPref.getLastNotifiedTimestamp(context)}")

            Log.d("threadviewmodel", "First run detected. Setting initial timestamp.")
        }

        Log.d("threadviewmodel", "Started listening for new threads") // Debug log

        threadRef.orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(
                        "ThreadViewModel",
                        "Initial threads loaded. Setting initialThreadsLoaded to true"
                    )
                    initialThreadsLoaded = true
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ThreadViewModel", "Error loading initial threads: ${error.message}")
                }
            })


        threadListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("threadviewmodel", "on child added mein aaya")  // Debug log

                // Access threadData inside threadId
                val threadData = snapshot.getValue(ThreadData::class.java) ?: return
                val threadTimestamp = threadData.timestamp
                val threadId = snapshot.key ?: return
                val threadTitle = threadData.thread
                val userId = threadData.uid ?: return

//                Log.d("threadviewmodel", " thread data : $threadTitle")  // Debug log


                if (!initialThreadsLoaded) {
                    Log.d("ThreadViewModel", "Initial threads still loading. Ignoring thread.")
                    return
                }

                if (threadTimestamp <= lastNotifiedTimestamp) {
//                    Log.d("threadviewmodel", "Thread filtered out due to timestamp")  // Debug log
                    return
                }

                if (SharedPref.isNotificationAcknowledged(context, threadId)) {
                    Log.d(
                        "ThreadViewModel",
                        "Notification already acknowledged for thread: $threadId"
                    )
                    return
                }


                Log.d("threadviewmodel", "else mein aa gaye")
                lastProcessedTimestamp = threadTimestamp


                viewModelScope.launch {
                    try {
                        // Fetch followers from Firestore
                        firestore.collection("Followers").document(userId).get()
                            .addOnSuccessListener { followersDoc ->
                                if (!followersDoc.exists()) {
                                    Log.d("threadviewmodel", "folllowers esixt nahi karta")
                                    return@addOnSuccessListener
                                }

                                Log.d("threadviewmodel", "user id : $userId")
                                val followers =
                                    followersDoc?.get("followers_id") as? List<String> ?: listOf()
                                Log.d("threadviewmodel", "list of followers : ${followers}")


                                followers.forEach { followerId ->
                                    Log.e("threadviewmodel", "follower id : ${followerId}")


                                    // Get OneSignal ID of the follower from Realtime Database
                                    db.getReference("users").child(followerId).child("oneSignalId")
                                        .get()
                                        .addOnSuccessListener { oneSignalSnapshot ->
                                            val oneSignalId =
                                                oneSignalSnapshot.getValue(String::class.java)
                                            Log.e(
                                                "threadviewmodel",
                                                "onesignal id : ${oneSignalId}"
                                            )
                                            if (!oneSignalId.isNullOrEmpty()) {
                                                Log.e(
                                                    "threadviewmodel",
                                                    "Sending notification to: $followerId ($oneSignalId)"
                                                ) // Debug log

                                                notificationHelper.sendNotification(
                                                    title = "New Thread Posted!",
                                                    message = "Check out the latest thread by : ${threadTitle}",
                                                    playerId = oneSignalId
                                                )
                                                // Update lastNotifiedTimestamp AFTER sending notifications
                                                lastProcessedTimestamp?.let {
                                                    lastNotifiedTimestamp = it
                                                    SharedPref.saveLastNotifiedTimestamp(
                                                        context,
                                                        lastNotifiedTimestamp
                                                    )
                                                    Log.d(
                                                        "ThreadViewModel",
                                                        "lastNotifiedTimestamp updated to: $lastNotifiedTimestamp"
                                                    )
                                                }

                                                SharedPref.markNotificationAcknowledged(
                                                    context,
                                                    threadId
                                                )
                                            }
                                        }
                                }
                            }.addOnFailureListener {
                                Log.e("OneSignal", "Failed to fetch followers", it) // Debug log

                            }

                    } catch (e: Exception) {
                        Log.d("ThreadViewModel", "error in fetchinf threads called ")
                    }
                }
            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ThreadViewModel", "onChildChanged called")

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("ThreadViewModel", "onChildRemoved called")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ThreadViewModel", "onChildMoved called")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ThreadViewModel", "data base errror : ${error.message}") // Debug log
                isListening = false
            }
        }
        threadRef.orderByChild("timestamp").addChildEventListener(threadListener!!)
        isListening = true
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the listener when the ViewModel is cleared
        if (threadListener != null) {
            threadListener?.let { threadRef.removeEventListener(it) }
            isListening = false
        }

        commentListener?.let {
            currentThreadId?.let { threadId ->
                threadRef.child(threadId).child("comments").removeEventListener(it)
            }
        }
    }
}
