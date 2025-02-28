package com.berlin.sample1.repository

import com.berlin.sample1.model.GeminiResponse
import com.berlin.sample1.model.Prompt
import com.berlin.sample1.network.GeminiApiService
import retrofit2.Response

class GeminiRepository(private val geminiApiService: GeminiApiService) {

    suspend fun sendMessage(apiKey: String, prompt: Prompt): Response<GeminiResponse> {
        return geminiApiService.sendMessage(apiKey, prompt)
    }
}