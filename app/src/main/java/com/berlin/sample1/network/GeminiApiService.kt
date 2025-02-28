package com.berlin.sample1.network

import com.berlin.sample1.model.GeminiResponse
import com.berlin.sample1.model.Prompt
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun sendMessage(
        @Query("key") apiKey: String,
        @Body prompt: Prompt
    ): Response<GeminiResponse>
}