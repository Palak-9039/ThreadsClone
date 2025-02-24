package com.example.finalproject.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.Model.ThreadData
import com.example.finalproject.Model.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class HomeViewModel : ViewModel() {
    var db = FirebaseDatabase.getInstance()
    var threadRef = db.getReference("threads")
    var users = db.getReference("users")


    private var _threadAndUser = MutableLiveData<List<Pair<ThreadData, User>>>()
    var threadAndUser: LiveData<List<Pair<ThreadData, User>>> = _threadAndUser

    private var _isLoading = MutableLiveData<Boolean>()
    var isLoading: LiveData<Boolean> = _isLoading


    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _isLoading.postValue(true)

                    val threadId = snapshot.key
                    val threadData = snapshot.getValue(ThreadData::class.java)
                    threadData?.let {

                        if (it.threadId == null) {
                            // Get a reference to the specific thread in the database
                            val threadRef = threadRef.child(threadId!!)

                            // Update the threadId in the database
                            threadRef.child("threadId").setValue(threadId).await()

                            Log.d(
                                "ThreadViewModel",
                                "threadId was null for thread: $threadId, set to: $threadId"
                            )
                        }

                        val likesSnapshot = snapshot.child("likes")
                        if (!likesSnapshot.exists()) {
                            // likes parameter is missing, add it as an empty map
                            println("Updating thread: $threadId - Adding likes parameter")
                            threadRef.child("likes").setValue(emptyMap<String, Boolean>()).await()
                        } else if (likesSnapshot.value is Boolean) {
                            // likes parameter is a boolean, delete it and add an empty map
                            println("Updating thread: $threadId - likes parameter is a boolean, deleting and adding an empty map")
                            threadRef.child("likes").removeValue().await()
                            threadRef.child("likes").setValue(emptyMap<String, Boolean>()).await()
                        }


                        val user = fetchUser(it.uid!!)
                        withContext(Dispatchers.Main) {
                            val currentList = _threadAndUser.value.orEmpty().toMutableList()
                            currentList.add(0, Pair(threadData, user))
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
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val threadId = snapshot.key
                    Log.d("onChildChanged", "Thread changed: $threadId")
                    val threadData = snapshot.getValue(ThreadData::class.java)
                    threadData?.let { updatedThreadData ->
                        Log.d("onChildChanged", "Updated thread data: $updatedThreadData")
                        // Find the index of the thread in the current list
                        val currentList = _threadAndUser.value.orEmpty().toMutableList()
                        val index = currentList.indexOfFirst { it.first.threadId == threadId }

                        if (index != -1) {
                            Log.d("onChildChanged", "Found thread at index: $index")
                            // Update the thread data in the list
                            val user = currentList[index].second
                            val updatedPair = Pair(updatedThreadData, user)
                            currentList[index] = updatedPair

                            // Update the LiveData on the main thread
                            withContext(Dispatchers.Main) {
                                Log.d("onChildChanged", "Updating LiveData with new list")
                                _threadAndUser.value = currentList.toList() // Create a new list
                            }
                        } else {
                            Log.w("onChildChanged", "Thread not found in list: $threadId")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Firebase", "Error in onChildChanged: ${e.message}")
                }
            }
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
        threadRef.addChildEventListener(childEventListener)
        fetchInitialThreads()
    }

    private fun fetchInitialThreads() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val snapshot = threadRef.orderByChild("timestamp").get().await()
                val threadUserList = mutableListOf<Pair<ThreadData, User>>()
                for (threadSnapshot in snapshot.children.reversed()) {
                    val threadId = threadSnapshot.key
                    val threadData = threadSnapshot.getValue(ThreadData::class.java)

                    threadData?.let {

                        val user = it.uid?.let { uid -> fetchUser(uid) }
                        if (user != null) {
                            threadUserList.add(it to user)
                        }
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


    fun refreshData() {
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
                    _threadAndUser.postValue(threadUserList)
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error refreshing threads: ${e.message}")
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
