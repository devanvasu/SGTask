package com.example.sktask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sktask.repository.ImageRepository
import com.example.sktask.response.ImageUploadResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageViewModel(private val repository: ImageRepository) : ViewModel() {
    private val _uploadMessage = MutableLiveData<String>()
    val uploadMessage: LiveData<String>
        get() = _uploadMessage
    fun uploadImage(imagePath: String, description: String) {

                val response = repository.uploadImage(imagePath, description)

            response.enqueue(object : Callback<ImageUploadResponse> {
                override fun onResponse(call: Call<ImageUploadResponse>, response: Response<ImageUploadResponse>) {
                    if (response.isSuccessful) {
                        // Handle success
                        val uploadResponse = response.body()
                        _uploadMessage.postValue("Image Uploaded Successfully")
                    } else {
                        _uploadMessage.postValue(" Not Image Uploaded Successfully")

                    }
                }

                override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                    // Handle network errors or exceptions
                }
            })

        }
}