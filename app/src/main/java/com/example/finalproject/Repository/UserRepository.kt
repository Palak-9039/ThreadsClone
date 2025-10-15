package com.example.finalproject.Repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.finalproject.ImageUploading.uploadImageToCloudinary
import com.example.finalproject.Model.SharedPref
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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




    suspend fun updatePasswordInDb(
        currentPassword : String,
        newPassword : String,
    ) : Result<Unit>{

        val user = FirebaseAuth.getInstance().currentUser ?: return Result.failure(Exception("User not logged in"))
        val email = user?.email ?: return Result.failure(Exception("No email found"))

        return try {
            val credentials = EmailAuthProvider.getCredential(email,currentPassword)

            user.reauthenticate(credentials).await()
            user.updatePassword(newPassword).await()

            return Result.success(Unit)
        }catch (e : Exception){
            Result.failure(e)
        }


    }


    suspend fun updateProfilePhoto(userId:String, imageRef: Uri?) : Result<String>{

      return try {

          if(imageRef == null){
             return Result.failure(IllegalArgumentException("ImageRef cannot be null"))
          }
            val url = uploadProfileImageToCloudinary(imageRef)
            updatePhotoInDb(userId, url)
            saveImage(url)

            Result.success(url)
        }catch (e : Exception){
            Result.failure(e)
        }

    }

    //update image in database
    private suspend fun updatePhotoInDb(userId : String, newPhotoUrl : String){
        db.child(userId)
            .child("imageUrl")
            .setValue(newPhotoUrl)
            .await()

    }



    // upload image to cloudinary
   private suspend fun uploadProfileImageToCloudinary(imageRef : Uri?): String =
        suspendCoroutine{ continuation ->

        uploadImageToCloudinary(
            context,
            imageRef!!,
            "296176452574737",
            "qQPbMgW7Uih0pGZY4NqHjIqbfiI",
            "dummy"
        ){ url ->
            if(url != null){
                Log.d("cloudinary response from profile","image uploaded successfully : ${url}")
                continuation.resume(url)
            } else {
                Log.d("cloudinary response from profile","Image upload failed") // Debugging log
                continuation.resumeWithException(Exception("Cloudinary upload failed"))
            }
        }
        }




}