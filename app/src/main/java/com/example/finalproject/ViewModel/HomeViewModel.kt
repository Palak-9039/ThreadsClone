package com.example.finalproject.ViewModel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class HomeViewModel : ViewModel() {
    var db = FirebaseDatabase.getInstance()
    var threadRef = db.getReference("threads")
    var users = db.getReference("users")


    private var _threadAndUser = MutableLiveData<List<Pair<ThreadData,User>>>()
    var threadAndUser: LiveData<List<Pair<ThreadData, User>>> = _threadAndUser

    private var _isLoading = MutableLiveData<Boolean>()
    var isLoading : LiveData<Boolean> = _isLoading


    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _isLoading.postValue(true)
                    val threadData = snapshot.getValue(ThreadData::class.java)
                    threadData?.let {
                        val user = fetchUser(it.uid!!)
                        withContext(Dispatchers.Main) {
                            val currentList = _threadAndUser.value.orEmpty().toMutableList()
                            currentList.add(0,Pair(threadData, user))
                            _threadAndUser.value = currentList
                            _isLoading.postValue(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Firebase", "Error in onChildAdded: ${e.message}")
                    _isLoading.postValue(false)
                }
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            // Handle changes to existing threads (if needed)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            // Handle thread removal (if needed)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            // Handle thread reordering (if needed)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Database error: ${error.message}")
            _isLoading.postValue(false)
        }
    }

    init {
        fetchInitialThreads()
        threadRef.addChildEventListener(childEventListener)
    }

    private fun fetchInitialThreads() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val snapshot = threadRef.orderByChild("timestamp").get().await()
                val threadUserList = mutableListOf<Pair<ThreadData, User>>()
                for (threadSnapshot in snapshot.children.reversed()) {
                    val threadData = threadSnapshot.getValue(ThreadData::class.java)
                    threadData?.let {
                        val user = fetchUser(it.uid!!)
                        threadUserList.add(it to user)
                    }
                }

                withContext(Dispatchers.Main) {
                    _threadAndUser.value = threadUserList
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching initial threads: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }


//    suspend fun fetchThreadAndUser() = withContext(Dispatchers.IO) {
//        try {
//            val snapshot = threadRef.get().await() // Fetch threads asynchronously
//            val resultList = mutableListOf<Deferred<Pair<ThreadData, User>>>()
//
//            for (threadSnapshot in snapshot.children) {
//                val thread = threadSnapshot.getValue(ThreadData::class.java)
//                thread?.let { threadData ->
//                    val deferredPair = async {
//                        val user = fetchUser(threadData.uid!!) // Fetch user concurrently
//                        threadData to user
//                    }
//                    resultList.add(deferredPair)
//                }
//            }
//
//            val threadUserList = resultList.awaitAll() // Wait for all user fetches to complete
//            _threadAndUser.postValue(threadUserList) // Update LiveData on UI thread
//        } catch (e: Exception) {
//            Log.e("Firebase", "Error fetching threads: ${e.message}")
//        }
//    }


    fun refreshData(){
        viewModelScope.launch (Dispatchers.IO){
            try {
                _isLoading.postValue(true)
                val snapshot = threadRef.orderByChild("timestamp").get().await()
                val threadUserList = mutableListOf<Pair<ThreadData,User>>()

                for (threadSnapshot in snapshot.children.reversed()){
                    val threadData = threadSnapshot.getValue(ThreadData::class.java)
                    threadData?.let {
                        val user = fetchUser(it.uid!!)
                        threadUserList.add(it to user)
                    }
                }
                withContext(Dispatchers.Main){
                    _threadAndUser.postValue(threadUserList)
                    _isLoading.postValue(false)
                }
            }catch (e:Exception){
                Log.e("Firebase","Error refreshing threads: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
    private suspend fun fetchUser(uid: String): User = withContext(Dispatchers.IO) {
        try {
            val snapshot = users.child(uid).get().await()
            snapshot.getValue(User::class.java) ?: User() // Return user or default
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching user: ${e.message}")
            User() // Return a default user if an error occurs
        }
    }


        override fun onCleared() {
            super.onCleared()
            threadRef.removeEventListener(childEventListener)
        }
    }
