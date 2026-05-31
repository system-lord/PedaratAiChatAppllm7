package com.example.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LLM7ApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse

    @POST("images/generations")
    suspend fun generateImage(
        @Header("Authorization") authHeader: String,
        @Body request: ImageGenerationRequest
    ): ImageGenerationResponse

    @GET("models")
    suspend fun listModels(
        @Header("Authorization") authHeader: String
    ): ModelListResponse
}
