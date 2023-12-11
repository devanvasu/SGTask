package com.example.sktask.retrofit

import com.example.sktask.response.ImageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageUploadService {
    @Multipart
    @POST("posts")
     fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<ImageUploadResponse>

}