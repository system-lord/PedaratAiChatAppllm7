package com.example.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionMessage(
    val role: String, // "user" or "assistant" or "system"
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatCompletionMessage>,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val id: String?,
    val model: String?,
    val choices: List<ChatChoice>?
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    val index: Int?,
    val message: ChatCompletionMessage,
    val finish_reason: String?
)

@JsonClass(generateAdapter = true)
data class ModelData(
    val id: String,
    val objectType: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelListResponse(
    val data: List<ModelData>
)

@JsonClass(generateAdapter = true)
data class ImageGenerationRequest(
    val prompt: String,
    val n: Int = 1,
    val size: String = "1024x1024"
)

@JsonClass(generateAdapter = true)
data class ImageGenerationResponse(
    val created: Long?,
    val data: List<ImageData>?
)

@JsonClass(generateAdapter = true)
data class ImageData(
    val url: String?
)
