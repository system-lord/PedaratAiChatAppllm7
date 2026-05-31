package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.db.ChatMessage
import com.example.data.db.ChatThread
import com.example.data.repository.ChatRepository
import com.example.ui.locale.AppLanguage
import com.example.ui.theme.AppThemeSetting
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    // Language state
    private val _language = MutableStateFlow(
        try { AppLanguage.valueOf(prefs.language) } catch (e: Exception) { AppLanguage.ENGLISH }
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Theme state
    private val _themeSetting = MutableStateFlow(
        try { AppThemeSetting.valueOf(prefs.theme) } catch (e: Exception) { AppThemeSetting.DARK }
    )
    val themeSetting: StateFlow<AppThemeSetting> = _themeSetting.asStateFlow()

    // API key state
    private val _apiKey = MutableStateFlow(prefs.apiKey)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    // Dynamic models list
    private val _availableModels = MutableStateFlow<List<String>>(repository.getFallbackFreeModels())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    // Selected Model ID
    private val _selectedModelId = MutableStateFlow(prefs.defaultModelId)
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    // Thread listing
    val allThreads: StateFlow<List<ChatThread>> = repository.allThreads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current thread ID selection
    private val _selectedThreadId = MutableStateFlow<Int?>(null)
    val selectedThreadId: StateFlow<Int?> = _selectedThreadId.asStateFlow()

    // Active message list
    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages.asStateFlow()

    // Generation loading flag
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Image-generation mode activated flag
    private val _isImageMode = MutableStateFlow(false)
    val isImageMode: StateFlow<Boolean> = _isImageMode.asStateFlow()

    // Sync Models status feedback
    private val _fetchStatus = MutableStateFlow<String?>(null)
    val fetchStatus: StateFlow<String?> = _fetchStatus.asStateFlow()

    // Active listening job for room database stream
    private var messagesJob: Job? = null

    init {
        // Initialize active model
        if (_apiKey.value.isNotEmpty()) {
            syncModels()
        }
        
        // Auto-select latest thread if available
        viewModelScope.launch {
            allThreads.collect { threads ->
                if (_selectedThreadId.value == null && threads.isNotEmpty()) {
                    selectThread(threads.first().id)
                }
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.language = lang.name
    }

    fun setTheme(theme: AppThemeSetting) {
        _themeSetting.value = theme
        prefs.theme = theme.name
    }

    fun setApiKey(key: String) {
        val trimmedKey = key.trim()
        _apiKey.value = trimmedKey
        prefs.apiKey = trimmedKey
        syncModels()
    }

    fun setImageMode(enabled: Boolean) {
        _isImageMode.value = enabled
    }

    fun selectModel(modelId: String) {
        _selectedModelId.value = modelId
        prefs.defaultModelId = modelId
        
        // If there's an active thread, let's update its configured model
        val currentThreadId = _selectedThreadId.value
        if (currentThreadId != null) {
            viewModelScope.launch {
                repository.updateThreadTitle(currentThreadId, "Chat with $modelId")
            }
        }
    }

    fun selectThread(threadId: Int?) {
        _selectedThreadId.value = threadId
        messagesJob?.cancel()
        if (threadId != null) {
            messagesJob = viewModelScope.launch {
                repository.getMessagesForThread(threadId).collect {
                    _currentMessages.value = it
                }
            }
        } else {
            _currentMessages.value = emptyList()
        }
    }

    fun createNewThread() {
        viewModelScope.launch {
            val title = "Chat with ${_selectedModelId.value}"
            val newId = repository.createThread(title, _selectedModelId.value)
            selectThread(newId)
        }
    }

    fun deleteThread(thread: ChatThread) {
        viewModelScope.launch {
            if (_selectedThreadId.value == thread.id) {
                _selectedThreadId.value = null
                _currentMessages.value = emptyList()
            }
            repository.deleteThread(thread)
        }
    }

    fun syncModels() {
        if (_apiKey.value.isEmpty()) return
        viewModelScope.launch {
            _fetchStatus.value = "LOADING"
            val modelsResult = repository.listModelsFromApi(_apiKey.value)
            if (modelsResult.isNotEmpty() && modelsResult != repository.getFallbackFreeModels()) {
                _availableModels.value = modelsResult
                _fetchStatus.value = "SUCCESS"
                if (!modelsResult.contains(_selectedModelId.value)) {
                    _selectedModelId.value = modelsResult.first()
                }
            } else {
                _fetchStatus.value = "FAILED"
            }
        }
    }

    fun clearStatusFeedback() {
        _fetchStatus.value = null
    }

    fun sendMessage(text: String, onError: (String) -> Unit) {
        val rawText = text.trim()
        if (rawText.isEmpty()) return
        
        val key = _apiKey.value
        if (key.isEmpty()) {
            onError("error_no_key")
            return
        }

        viewModelScope.launch {
            // Ensure we have an active thread
            var activeThreadId = _selectedThreadId.value
            if (activeThreadId == null) {
                val title = if (_isImageMode.value) "Image: $rawText" else "Chat with ${_selectedModelId.value}"
                activeThreadId = repository.createThread(title, _selectedModelId.value)
                selectThread(activeThreadId)
            }

            // Insert user message to local Room DB
            val userMsg = ChatMessage(threadId = activeThreadId, role = "user", content = rawText)
            repository.insertMessage(userMsg)

            _isGenerating.value = true

            try {
                if (_isImageMode.value) {
                    val imageUrl = repository.generateImage(apiKey = key, prompt = rawText)
                    val assistantMsg = ChatMessage(threadId = activeThreadId, role = "assistant", content = "IMAGE_URL:$imageUrl")
                    repository.insertMessage(assistantMsg)
                } else {
                    // Fetch context history for completions call
                    val history = _currentMessages.value + userMsg
                    val aiReply = repository.generateChatCompletion(
                        apiKey = key,
                        modelId = _selectedModelId.value,
                        history = history
                    )
                    
                    // Write assistant reply to Room DB
                    val assistantMsg = ChatMessage(threadId = activeThreadId, role = "assistant", content = aiReply)
                    repository.insertMessage(assistantMsg)
                }
            } catch (e: Exception) {
                // Record error response safely
                val errMsg = ChatMessage(
                    threadId = activeThreadId, 
                    role = "assistant", 
                    content = "Error connecting to LLM7: ${e.message ?: "Unknown API exception"}"
                )
                repository.insertMessage(errMsg)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    class Factory(
        private val repository: ChatRepository,
        private val prefs: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(repository, prefs) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
