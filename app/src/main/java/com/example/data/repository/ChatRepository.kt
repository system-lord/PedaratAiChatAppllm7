package com.example.data.repository

import com.example.data.api.ChatCompletionMessage
import com.example.data.api.ChatCompletionRequest
import com.example.data.api.ImageGenerationRequest
import com.example.data.api.LLM7ApiService
import com.example.data.db.ChatDao
import com.example.data.db.ChatMessage
import com.example.data.db.ChatThread
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: LLM7ApiService
) {
    val allThreads: Flow<List<ChatThread>> = chatDao.getAllThreads()

    fun getMessagesForThread(threadId: Int): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForThread(threadId)
    }

    suspend fun createThread(title: String, modelId: String): Int {
        val thread = ChatThread(title = title, modelId = modelId)
        return chatDao.insertThread(thread).toInt()
    }

    suspend fun updateThreadTitle(threadId: Int, title: String) {
        chatDao.updateThreadTitle(threadId, title)
    }

    suspend fun deleteThread(thread: ChatThread) {
        chatDao.deleteThread(thread)
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        return chatDao.insertMessage(message)
    }

    suspend fun clearMessages(threadId: Int) {
        chatDao.clearMessagesForThread(threadId)
    }

    suspend fun listModelsFromApi(apiKey: String): List<String> {
        return try {
            val authHeader = "Bearer ${apiKey.trim()}"
            val response = apiService.listModels(authHeader)
            response.data.map { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackFreeModels()
        }
    }

    suspend fun generateChatCompletion(
        apiKey: String,
        modelId: String,
        history: List<ChatMessage>
    ): String {
        val authHeader = "Bearer ${apiKey.trim()}"
        val messages = history.map {
            ChatCompletionMessage(
                role = it.role,
                content = it.content
            )
        }
        val request = ChatCompletionRequest(
            model = modelId,
            messages = messages
        )
        val response = apiService.createChatCompletion(authHeader, request)
        return response.choices?.firstOrNull()?.message?.content 
            ?: throw Exception("No choice returned from completion API")
    }

    suspend fun generateImage(
        apiKey: String,
        prompt: String
    ): String {
        val authHeader = "Bearer ${apiKey.trim()}"
        val request = ImageGenerationRequest(prompt = prompt)
        val response = apiService.generateImage(authHeader, request)
        return response.data?.firstOrNull()?.url 
            ?: throw Exception("No image URL returned from API")
    }

    fun getFallbackFreeModels(): List<String> {
        return listOf(
            "gpt-4o-mini",
            "gemini-1.5-flash",
            "deepseek-chat",
            "deepseek-coder",
            "meta-llama/llama-3-8b-instruct",
            "meta-llama/llama-3.1-8b-instruct",
            "mistral-7b-instruct",
            "gemma-2-9b-it"
        )
    }
}
