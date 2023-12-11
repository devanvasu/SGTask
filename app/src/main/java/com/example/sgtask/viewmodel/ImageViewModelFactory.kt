package com.example.sktask.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sktask.repository.ImageRepository
import com.example.sktask.retrofit.ImageUploadService

class ImageViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}