package com.example.finalproject.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.onesignal.OneSignal
import com.onesignal.OneSignal.OSExternalUserIdUpdateCompletionHandler

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val userRef = database.getReference("users")

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    var firebaseUser: LiveData<FirebaseUser?> = _firebaseUser

    private var _error = MutableLiveData<String>()
    var error: LiveData<String> = _error

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

    fun login(email: String, password: String, context: Context) {
        _isLoading.postValue(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _isLoading.value = false
                if (it.isSuccessful) {
                    _firebaseUser.value = auth.currentUser
                    getData(auth.currentUser!!.uid, context)
                    if (_firebaseUser != null) {
                        onUserLoggedIn(_firebaseUser.value!!.uid)
                    }
                } else {
                    _error.value = it.exception?.message ?: "Login failed"
                    Log.e("AuthViewModel", "Login failed: ${it.exception?.message}")
                }
            }
    }

    private fun getData(uid: String, context: Context) {
        _isLoading.postValue(true)

        userRef.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isLoading.postValue(false)

                    if (snapshot.exists()) {
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
                            Log.d(
                                "AuthViewModel",
                                "User data retrieved and stored successfully: $uid"
                            )
                        } else {
                            Log.e("AuthRepository", "User data is null for user: $uid")
                            _error.value = "User data is null"
                        }
                    } else {
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

    fun register(
        email: String,
        password: String,
        name: String,
        userName: String,
        imageUrl: String?,
        context: Context
    ) {

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                println("register funciton mein aaya")
                _isLoading.value = false
                if (it.isSuccessful) {
                    _firebaseUser.postValue(auth.currentUser)
                    saveData(
                        email,
                        password,
                        name,
                        userName,
                        auth.currentUser?.uid,
                        imageUrl,
                        context
                    )
                    if (_firebaseUser != null) {
                        onUserLoggedIn(_firebaseUser.value!!.uid)
                    }
                } else {
                    _error.value = it.exception?.message ?: "Registration failed"
                    Log.e("AuthViewModel", "Registration failed: ${it.exception?.message}")
                }
            }
    }

    private fun saveData(
        email: String,
        password: String,
        name: String,
        userName: String,
        uid: String?,
        imageUrl: String?,
        context: Context
    ) {
        _isLoading.postValue(true)

        val userObject = User(name, userName, email, password, uid, imageUrl)

        userRef.child(uid!!).setValue(userObject)
            .addOnSuccessListener {
                _isLoading.postValue(false)

                SharedPref.storeData(name, userName, email, uid, imageUrl, context)
                Log.d("AuthViewModel", "User data saved successfully for user: $uid")
            }
            .addOnFailureListener { exception ->
                _isLoading.postValue(false)

                _error.value = exception.message ?: "Failed to save user data"
                Log.e("AuthViewModel", "Failed to save user data: ${exception.message}")
            }
    }

    fun saveOneSignalIdToDatabase(userId: String, oneSignalId: String) {
        val db = FirebaseDatabase.getInstance()
        val userRef = db.getReference("users").child(userId)
        userRef.child("oneSignalId").setValue(oneSignalId)
            .addOnSuccessListener {
                Log.d("OneSignal", "OneSignal ID saved for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("OneSignal", "Failed to save OneSignal ID", e)
            }
    }

    fun onUserLoggedIn(userId: String) {
        OneSignal.getDeviceState()?.userId?.let { oneSignalId ->
            Log.d("OneSignal", "OneSignal ID: $oneSignalId")
            OneSignal.setExternalUserId(userId) // This is the key line!
            Log.d("OneSignal", "External User ID set to: $userId")
            saveOneSignalIdToDatabase(userId, oneSignalId)
        }
    }

    fun SignOut() {

        val userId = auth.currentUser?.uid

        OneSignal.removeExternalUserId(object : OSExternalUserIdUpdateCompletionHandler {
            override fun onSuccess(results: org.json.JSONObject) {
                Log.i("OneSignal", "Successfully removed external user ID: $results")
                // Now, sign out of Firebase Authentication
                auth.signOut()
                _firebaseUser.postValue(null)
                Log.d("OneSignal", "Firebase logout called")

                // Remove the onesignal id from the database
                if (userId != null) {
                    removeOneSignalIdFromDatabase(userId)
                }
            }

            override fun onFailure(error: OneSignal.ExternalIdError) {
                Log.e("OneSignal", "Failed to remove external user ID: ${error.message}")
            }
        })
    }


    fun removeOneSignalIdFromDatabase(userId: String) {
        val db = FirebaseDatabase.getInstance()
        val userRef = db.getReference("users").child(userId)
        userRef.child("oneSignalId").removeValue()
            .addOnSuccessListener {
                Log.d("OneSignal", "OneSignal ID removed from database for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("OneSignal", "Failed to remove OneSignal ID from database", e)
            }
    }
}