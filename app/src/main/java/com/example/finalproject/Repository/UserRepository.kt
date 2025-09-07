package com.example.finalproject.Repository

import android.content.Context
import android.util.Log
import com.example.finalproject.Model.SharedPref
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepository(private val context : Context) {

    private val db = FirebaseDatabase.getInstance().getReference("users")

    fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun getName() : String = SharedPref.getName(context)
    fun saveName(name : String) = SharedPref.saveName(context,name)

    fun getUserName() : String = SharedPref.getUserName(context)
    fun saveUserName(userName : String) = SharedPref.saveUsername(context,userName)

    fun getImage(): String? = SharedPref.getImage(context)
    fun saveImage(url : String) = SharedPref.saveImage(context,url)



    fun updateNameInDb(userId : String, newName : String, onDone: ()-> Unit){
        db.child(userId).child("name").setValue(newName)
            .addOnSuccessListener {
                saveName(newName)
                onDone()
            }
    }

    fun updateUserNameInDb(userId : String, newUserName : String, onDone: ()-> Unit){
        db.child(userId).child("userName").setValue(newUserName)
            .addOnSuccessListener {
                saveUserName(newUserName)
                onDone()
            }
    }

    fun updatePhotoInDb(userId : String, newPhotoUrl : String, onDone: ()-> Unit){
        db.child(userId).child("imageUrl").setValue(newPhotoUrl)
            .addOnSuccessListener {
                saveImage(newPhotoUrl)
                onDone()
            }
            .addOnFailureListener{
                Log.e("update photo","photo couldn't be updated in database",it)
            }
    }


    fun updatePasswordInDb(
        currentPassword : String,
        newPassword : String,
        onSuccess : () -> Unit,
        onError : (String)-> Unit
    ){
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val email = user?.email ?: return

        val credentials = EmailAuthProvider.getCredential(email,currentPassword)

        user.reauthenticate(credentials).addOnCompleteListener{reauthTask ->
            if(reauthTask.isSuccessful){
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if(updateTask.isSuccessful){
                        onSuccess()
                    }else{
                        onError(updateTask.exception?.message ?: "Failed to update password")
                    }
                }
            }else{
                onError(reauthTask.exception?.message ?: "Re-authentication failed")
            }
        }
    }


}