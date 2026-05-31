package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.data.PreferencesManager
import com.example.data.api.LLM7ApiClient
import com.example.data.db.AppDatabase
import com.example.data.repository.ChatRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.PedaratAiTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize local shared preferences holding keys, themes and system states
        val prefs = PreferencesManager(applicationContext)

        // Initialize room local database with threads and messages schema
        val database = AppDatabase.getDatabase(applicationContext)

        // Bind our database queries and network services into repository layer
        val repository = ChatRepository(database.chatDao(), LLM7ApiClient.service)

        // Instantiate core viewmodel holding the visual and chat state machines
        val factory = ChatViewModel.Factory(repository, prefs)
        val viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            val themeSetting by viewModel.themeSetting.collectAsState()

            // Root Application Material Presets
            PedaratAiTheme(themeSetting = themeSetting) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
