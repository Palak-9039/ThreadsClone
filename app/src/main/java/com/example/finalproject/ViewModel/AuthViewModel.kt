package com.example.finalproject.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val userRef = database.getReference("users")

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    var firebaseUser : LiveData<FirebaseUser?> = _firebaseUser

    private var _error = MutableLiveData<String>()
    var error : LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            _firebaseUser.value = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(authStateListener)
    }

     fun login(email : String,password : String,context: Context){
         _isLoading.postValue(true)
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                _isLoading.value = false
                if(it.isSuccessful){
                    _firebaseUser.value = auth.currentUser
                    getData(auth.currentUser!!.uid,context)
                }else{
                    _error.value = it.exception?.message?: "Login failed"
                    Log.e("AuthViewModel", "Login failed: ${it.exception?.message}")
                }
            }
    }

    private fun getData(uid: String, context: Context) {
        _isLoading.postValue(true)

        userRef.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                _isLoading.postValue(false)

                if(snapshot.exists()) {
                    var data = snapshot.getValue(User::class.java)

                    if (data != null) {
                        SharedPref.storeData(
                            data!!.name,
                            data!!.userName,
                            data!!.email,
                            uid,
                            data!!.imageUrl,
                            context
                        )
                        Log.d("AuthViewModel", "User data retrieved and stored successfully: $uid")
                    }else{
                        Log.e("AuthRepository", "User data is null for user: $uid")
                        _error.value = "User data is null"
                    }
                }else{
                    Log.e("AuthRepository", "User data does not exist for user: $uid")
                    _error.value = "User data does not exist"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.postValue(false)

                _error.value = error.message
                Log.e("AuthViewModel", "Failed to get user data: ${error.message}")
            }

        })
    }

    fun register(email: String,
                 password: String,
                 name :String,
                 userName : String,
                 imageUrl : String?,
                 context:Context){

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                println("register funciton mein aaya")
                _isLoading.value = false
                if(it.isSuccessful){
                    _firebaseUser.postValue(auth.currentUser)
                    saveData(email,password,name,userName,auth.currentUser?.uid,imageUrl,context)

                }else{
                    _error.value = it.exception?.message ?: "Registration failed"
                    Log.e("AuthViewModel", "Registration failed: ${it.exception?.message}")
                }
            }
    }

    private fun saveData(email: String, password: String, name: String, userName: String, uid: String?,imageUrl: String?,context: Context) {
        _isLoading.postValue(true)

        val userObject = User(name,userName,email,password,uid,imageUrl)

        userRef.child(uid!!).setValue(userObject)
            .addOnSuccessListener {
                _isLoading.postValue(false)

                SharedPref.storeData(name,userName,email,uid,imageUrl,context)
                Log.d("AuthViewModel", "User data saved successfully for user: $uid")
            }
            .addOnFailureListener{exception ->
                _isLoading.postValue(false)

                _error.value = exception.message ?: "Failed to save user data"
                Log.e("AuthViewModel", "Failed to save user data: ${exception.message}")
            }
    }

//     fun saveOneSignalPlayerId() = viewModelScope.launch {
//        OneSignal.getDeviceState()?.let { state ->
//            if (state.isSubscribed) {
//                val playerId = state.userId  // OneSignal Player ID
//                val userId = auth.currentUser?.uid ?: return@launch
//
//                // Store OneSignal Player ID in Firestore
//                database.getReference("users").child(userId)
//                    .child("oneSignalId")
//                    .setValue(playerId)
//                    .addOnSuccessListener { Log.d("OneSignal", "Player ID saved!")  }
//                    .addOnFailureListener{Log.d("OneSignal", "Failed to save Player ID", it) }
//            }
//        }
//    }


    fun saveOneSignalIdToDatabase(userId: String, oneSignalId: String) {
        Log.d("OneSignal", "saveOneSignalIdToDatabase: Attempting to save oneSignalId: $oneSignalId for userId: $userId")

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.child("oneSignalId").setValue(oneSignalId)
            .addOnSuccessListener {
                Log.d("OneSignal", "saveOneSignalIdToDatabase: OneSignal ID saved successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("OneSignal", "saveOneSignalIdToDatabase: Failed to save OneSignal ID for user: $userId", e)
            }
    }


    fun SignOut(){
        auth.signOut()
        FirebaseAuth.getInstance().signOut()
        _firebaseUser.postValue(null)
    }

}