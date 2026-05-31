package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pedarat_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "llm7_api_key"
        private const val KEY_THEME = "app_theme" // "LIGHT", "DARK", "COLORFUL"
        private const val KEY_LANGUAGE = "app_language" // "ENGLISH", "PERSIAN"
        private const val KEY_DEFAULT_MODEL = "default_model_id"
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var theme: String
        get() = prefs.getString(KEY_THEME, "DARK") ?: "DARK"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "ENGLISH") ?: "ENGLISH"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var defaultModelId: String
        get() = prefs.getString(KEY_DEFAULT_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = prefs.edit().putString(KEY_DEFAULT_MODEL, value).apply()
}
