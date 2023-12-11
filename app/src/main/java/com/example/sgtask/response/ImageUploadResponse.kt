package com.example.sktask.response

import com.google.gson.annotations.SerializedName

data class ImageUploadResponse(
    @SerializedName("url")
    val url: String?,
    @SerializedName("success")
    val success: Boolean?


)
