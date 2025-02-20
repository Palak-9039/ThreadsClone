package com.example.finalproject.ImageUploading

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest

fun generateSignature(params: Map<String, String>, apiSecret: String): String {
    val sortedParams = params.toSortedMap()
    val paramString = sortedParams.entries.joinToString("&") { "${it.key}=${it.value}" }
    val stringToSign = "$paramString$apiSecret"
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(stringToSign.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}


fun uploadImageToCloudinary(
    context: Context,
    imageUri: Uri,
    apiKey: String,
    apiSecret: String,
    uploadPreset: String,
    onResult : (String?) -> Unit
) {

    // Convert URI to File
    val inputStream = context.contentResolver.openInputStream(imageUri)
    val file = File.createTempFile("upload", ".jpg", context.cacheDir)
    println("file " + file)
    file.outputStream().use { inputStream?.copyTo(it) }

    // Generate Cloudinary Signature
    val timestamp = (System.currentTimeMillis() / 1000).toString()
    val signatureParams = mapOf(
        "timestamp" to timestamp,
        "upload_preset" to uploadPreset
    )
    val signature = generateSignature(signatureParams, apiSecret)
    println("signature " + signature)


    // Create MultipartBody for the image
    val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
    println("requestBody " + requestBody)
    val multipartBody =
        MultipartBody.Part.createFormData("file", file.name, requestBody)
    println("multipartBody " + multipartBody)

//    val presetRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), uploadPreset)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Cloudinary API
            val response = RetrofitClient.getClient().uploadImage(
                "dtpabbz9d",
                multipartBody,
                uploadPreset.toRequestBody("text/plain".toMediaTypeOrNull()),
                apiKey.toRequestBody("text/plain".toMediaTypeOrNull()),
                timestamp.toRequestBody("text/plain".toMediaTypeOrNull()),
                signature.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if(response.isSuccessful){
                val cloudinaryResponse = response.body()
                val secure_url = cloudinaryResponse?.secure_url
                withContext(Dispatchers.Main) {
                    println("secure url :"+ secure_url)
                    onResult(secure_url)
                }
//                onResult(secure_url)
            }else{
                Log.e("Cloudinary", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("idhar aayi problem")
            Log.e("Cloudinary", "Exception: ${e.message}")
        }
    }
}