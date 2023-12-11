package com.example.sktask.repository

import com.example.sktask.response.ImageUploadResponse
import com.example.sktask.retrofit.ImageUploadService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

class ImageRepository(private val apiService: ImageUploadService) {

     fun uploadImage(imagePath: String,description: String): Call<ImageUploadResponse> {
         val imageFile = File(imagePath)
         val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
         val imageBody = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
         val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.uploadImage(imageBody,descriptionBody)
    }
}