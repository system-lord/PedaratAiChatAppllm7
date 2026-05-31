package com.example.ui.locale

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    PERSIAN("fa", "فارسی", true)
}

object Localizer {
    private val en = mapOf(
        "app_name" to "Pedarat Ai",
        "chat" to "Chat",
        "settings" to "Settings",
        "welcome_title" to "Meet Pedarat AI",
        "welcome_subtitle" to "Smart gateway with all free llm7.io models",
        "model" to "Model",
        "placeholder_chat" to "Type a message...",
        "placeholder_image" to "Describe the image to generate...",
        "clear_chat" to "Clear History",
        "delete_thread" to "Delete Thread",
        "new_chat" to "New Chat",
        "no_messages" to "No messages yet. Ask me anything!",
        "settings_title" to "Configuration",
        "api_key_label" to "LLM7 API Key",
        "api_key_placeholder" to "Paste your key here...",
        "save_key" to "Save API Key",
        "key_saved" to "API Key saved successfully!",
        "theme_label" to "Visual Style",
        "theme_light" to "White Theme",
        "theme_dark" to "Dark Theme",
        "theme_colorful" to "Colorful Theme",
        "language_label" to "System Language",
        "get_key_title" to "How to get a free API Key?",
        "get_key_desc" to "1. Visit website token.llm7.io\n2. Log in and copy your permanent API token\n3. Use it to chat with dozens of premium AI models completely for free!",
        "fetch_models" to "Refresh Model List",
        "fetch_models_success" to "Models synced successfully!",
        "fetch_models_failed" to "Failed to sync. Check key/connection.",
        "anonymous_notice" to "Note: Ensure you enter your llm7 key to enable all features.",
        "threads" to "Conversations",
        "no_threads" to "No conversations yet. Create one!",
        "api_key_secured" to "Key Secured",
        "api_key_secured_desc" to "Your key is stored locally on this device and never shared elsewhere.",
        "error_no_key" to "Please save an API Key in settings first."
    )

    private val fa = mapOf(
        "app_name" to "پدرت آی‌آی",
        "chat" to "گفتگو",
        "settings" to "تنظیمات",
        "welcome_title" to "به پدرت آی‌آی خوش آمدید",
        "welcome_subtitle" to "دروازه هوشمند با دسترسی به تمام مدل‌های رایگان llm7.io",
        "model" to "مدل انتخابی",
        "placeholder_chat" to "پیام خود را اینجا بنویسید...",
        "placeholder_image" to "تصویر موردنظر خود را توصیف کنید...",
        "clear_chat" to "حذف پیام‌ها",
        "delete_thread" to "حذف این گفتگو",
        "new_chat" to "گفتگوی جدید",
        "no_messages" to "هیچ پیامی ارسال نشده است. بپرس!",
        "settings_title" to "پیکربندی برنامه",
        "api_key_label" to "کلید اختصاصی API LLM7",
        "api_key_placeholder" to "کلید کپی‌شده را اینجا جای‌گذاری کنید...",
        "save_key" to "ذخیره کلید API",
        "key_saved" to "کلید با موفقیت ذخیره شد!",
        "theme_label" to "قالب ظاهری و پوسته",
        "theme_light" to "پوسته سفید (روشن)",
        "theme_dark" to "پوسته تاریک (صنعتی)",
        "theme_colorful" to "پوسته رنگارنگ (شاد)",
        "language_label" to "زبان برنامه",
        "get_key_title" to "چگونه کلید API رایگان بگیریم؟",
        "get_key_desc" to "۱. به وب‌سایت token.llm7.io بروید\n۲. وارد شوید و شناسه توکن دائم خود را کپی کنید\n۳. آن را اینجا وارد کنید تا بتوانید با ده‌ها هوش مصنوعی رایگان گفتگو کنید!",
        "fetch_models" to "بروزرسانی لیست مدل‌ها",
        "fetch_models_success" to "لیست مدل‌ها با موفقیت هماهنگ شد!",
        "fetch_models_failed" to "خطا در هماهنگ‌سازی. کلید یا اینترنت خود را چک کنید.",
        "anonymous_notice" to "توجه: برای دسترسی کامل حتماً کلید خود را ذخیره کنید.",
        "threads" to "تاریخچه گفتگوها",
        "no_threads" to "هیچ گفتگویی وجود ندارد. یکی بسازید!",
        "api_key_secured" to "ذخیره امن اطلاعات",
        "api_key_secured_desc" to "کلید شما کاملاً محلی ذخیره می‌شود و هرگز به سرور فرعی ارسال نمی‌شود.",
        "error_no_key" to "لطفاً ابتدا کلید API خود را در بخش تنظیمات ذخیره کنید."
    )

    fun get(key: String, language: AppLanguage): String {
        val dict = if (language == AppLanguage.PERSIAN) fa else en
        return dict[key] ?: en[key] ?: key
    }
}
