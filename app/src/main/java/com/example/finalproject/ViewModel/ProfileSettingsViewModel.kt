package com.example.finalproject.ViewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.finalproject.Model.ProfileUiState
import com.example.finalproject.Model.SharedPref
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileSettingsViewModel(
    private val context : Context
) : ViewModel() {

    private val _uiState = mutableStateOf(ProfileUiState())
    val uiState : State<ProfileUiState> = _uiState

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseRef = FirebaseDatabase.getInstance().getReference("users")

    init {
        //handle this later if(userId == null)

        _uiState.value = ProfileUiState(
            photoUrl = SharedPref.getImage(context),
            userName = SharedPref.getUserName(context),
            name = SharedPref.getName(context)
        )

        if (userId != null) {

        databaseRef.child(userId).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newUrl = snapshot.child("imageUrl").getValue(String::class.java)
                    val newUserName = snapshot.child("userName").getValue(String::class.java)
                    val newName = snapshot.child("name").getValue(String::class.java)

                    _uiState.value = _uiState.value.copy(
                        photoUrl = newUrl ?: uiState.value.photoUrl,
                        userName = newUserName ?: uiState.value.userName,
                        name = newName ?: uiState.value.name
                    )

                    newUrl?.let { SharedPref.saveImage(context, it) }
                    newUserName?.let { SharedPref.saveUsername(context, it) }
                    newName?.let { SharedPref.saveName(context, it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileVM", "Failed to fetch user data", error.toException())
                }
            }
        )
    }
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
        userId?.let {
            databaseRef.child(it)
                .child("imageUrl")
                .setValue(newPhotoUrl)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        Log.d("update photo","photo updated in database ${newPhotoUrl}")
                        SharedPref.saveImage(context,newPhotoUrl)
                        _uiState.value = _uiState.value.copy(photoUrl = newPhotoUrl)
                    }else{
                        Log.d("update photo","photo couldn't be updated in database")
                    }

                }
        }
    }

    fun updateUserName(newUserName : String){
        userId?.let {
            databaseRef.child(it)
                .child("userName")
                .setValue(newUserName)
                .addOnSuccessListener {
                    SharedPref.saveUsername(context,newUserName)
                    _uiState.value = _uiState.value.copy(userName = newUserName)
                }
                .addOnFailureListener{
                    Log.e("update username","error in updating username ${it.message}",it)
                    Toast.makeText(context,"username ${it.message}",Toast.LENGTH_LONG).show()
                }
        }
    }

    fun updateName(newName : String){
        userId?.let {
            databaseRef.child(it)
                .child("name")
                .setValue(newName)
                .addOnSuccessListener {
                    SharedPref.saveName(context,newName)
                    _uiState.value = _uiState.value.copy(name = newName)
                }
                .addOnFailureListener{
                    Log.e("update name","error in updating name ${it.message}",it)
                    Toast.makeText(context,"name ${it.message}",Toast.LENGTH_LONG).show()
                }
        }
    }



    fun updatePassword(currentPassword : String,
                       newPassword : String){

        userId?.let {

            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email ?: return

            val credentials = EmailAuthProvider.getCredential(email,currentPassword)

            user.reauthenticate(credentials).addOnCompleteListener{reauthTask ->

                if(reauthTask.isSuccessful){
                    user.updatePassword(newPassword)
                        .addOnCompleteListener{
                            updateTask ->
                            if(updateTask.isSuccessful){
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context, "Failed to update password: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }else{
                    Toast.makeText(context, "Re-authentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}