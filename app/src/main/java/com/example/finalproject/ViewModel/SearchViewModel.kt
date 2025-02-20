package com.example.finalproject.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.Model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {
    var db = FirebaseDatabase.getInstance()
    var users = db.getReference("users")

    private var _userData = MutableLiveData<List<User>>()
    var userData : LiveData<List<User>> = _userData

    init {
        viewModelScope.launch {
            fetchUsers()
            println("users "+ _userData)
        }
    }

    suspend fun fetchUsers() = withContext(Dispatchers.IO){
        try{
            val snapshot = users.get().await()
            var resultList = mutableListOf<User>()

            for(userSnapshot in snapshot.children){
                val user = userSnapshot.getValue(User::class.java)
                user?.let {
                    resultList.add(user)
                }
            }

            val userList = resultList
            _userData.postValue(userList)
        }catch (e : Exception){
            Log.e("Firebase", "Error fetching threads: ${e.message}")
        }
    }
}