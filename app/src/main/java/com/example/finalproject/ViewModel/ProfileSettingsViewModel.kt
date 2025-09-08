package com.example.finalproject.ViewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.finalproject.Model.ProfileUiState
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.Repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ProfileSettingsViewModel(
    private val repo : UserRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(ProfileUiState())
    val uiState : State<ProfileUiState> = _uiState

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseRef = FirebaseDatabase.getInstance().getReference("users")

    init {
        //handle this later if(userId == null)

        _uiState.value = ProfileUiState(
            photoUrl = repo.getImage(),
            userName = repo.getUserName(),
            name = repo.getName()
        )

//        if (userId != null) {
//
//        databaseRef.child(userId).addValueEventListener(
//            object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val newUrl = snapshot.child("imageUrl").getValue(String::class.java)
//                    val newUserName = snapshot.child("userName").getValue(String::class.java)
//                    val newName = snapshot.child("name").getValue(String::class.java)
//
//                    _uiState.value = _uiState.value.copy(
//                        photoUrl = newUrl ?: uiState.value.photoUrl,
//                        userName = newUserName ?: uiState.value.userName,
//                        name = newName ?: uiState.value.name
//                    )
//
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("ProfileVM", "Failed to fetch user data", error.toException())
//                }
//            }
//        )
//    }
    }


    //updating UI state

    fun onPhotoUrlChanged(photoUrl : String){
        _uiState.value = _uiState.value.copy(photoUrl = photoUrl)
    }
    fun onUserNameChanged(userName : String){
        _uiState.value = _uiState.value.copy(userName = userName)
    }
    fun onNameChanged(name : String){
        _uiState.value = _uiState.value.copy(name = name)
    }




    // updating user profile details

    fun updatePhoto(newPhotoUrl : String){
        val userId = repo.getUserId() ?: return

        if(!newPhotoUrl.isNullOrEmpty()){
            repo.updatePhotoInDb(userId,newPhotoUrl){
                _uiState.value = _uiState.value.copy(photoUrl = newPhotoUrl)
            }
        }
    }

    fun updateUserName(newUserName : String){
        val userId = repo.getUserId() ?: return
        if(newUserName.isNotEmpty()){
        repo.updateUserNameInDb(userId,newUserName){
            _uiState.value = _uiState.value.copy(
                userName = newUserName
            )
        }
        }
    }

    fun updateName(newName : String){
       val userId = repo.getUserId() ?: return

        if(newName.isNotEmpty()){
            repo.updateNameInDb(userId,newName){
               _uiState.value = _uiState.value.copy(
                    name = newName
                )
            }
        }
    }



    fun updatePassword(currentPassword : String,
                       newPassword : String){
        viewModelScope.launch {
            val result = repo.updatePasswordInDb(currentPassword,newPassword)

                if(result.isSuccess){
                    _uiState.value = _uiState.value.copy(message = "Password updated successfully")
                }else{
                    _uiState.value = _uiState.value
                        .copy(message = "Error : ${result.exceptionOrNull()?.message}")
                }
        }
    }

    fun clearMessage(){
        _uiState.value = _uiState.value.copy(message = null)
    }


}