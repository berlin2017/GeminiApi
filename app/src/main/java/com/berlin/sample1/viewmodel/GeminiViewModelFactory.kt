package com.berlin.sample1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.berlin.sample1.repository.GeminiRepository

class GeminiViewModelFactory(private val geminiRepository: GeminiRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeminiViewModel(geminiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}