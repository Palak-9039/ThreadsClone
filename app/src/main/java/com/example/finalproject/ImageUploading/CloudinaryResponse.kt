package com.example.finalproject.ImageUploading

data class CloudinaryResponse(
    val secure_url: String,
    val public_id: String,
    val format: String,
    val bytes: Int
)
