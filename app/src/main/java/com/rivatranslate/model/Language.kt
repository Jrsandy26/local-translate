package com.rivatranslate.model

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String,
    val isDownloaded: Boolean = false
) {
    companion object {
        val ALL_LANGUAGES = listOf(
            Language("en", "English", "English", "🇺🇸", true),
            Language("ja", "Japanese", "日本語", "🇯🇵", true),
            Language("es", "Spanish", "Español", "🇪🇸", true),
            Language("fr", "French", "Français", "🇫🇷", true),
            Language("de", "German", "Deutsch", "🇩🇪", false),
            Language("zh", "Chinese", "中文", "🇨🇳", false),
            Language("ko", "Korean", "한국어", "🇰🇷", false),
            Language("it", "Italian", "Italiano", "🇮🇹", false),
            Language("pt", "Portuguese", "Português", "🇵🇹", false),
            Language("ru", "Russian", "Русский", "🇷🇺", false),
            Language("hi", "Hindi", "हिन्दी", "🇮🇳", false),
            Language("ar", "Arabic", "العربية", "🇸🇦", false),
            Language("tr", "Turkish", "Türkçe", "🇹🇷", false),
            Language("vi", "Vietnamese", "Tiếng Việt", "🇻🇳", false),
            Language("th", "Thai", "ไทย", "🇹🇭", false),
            Language("nl", "Dutch", "Nederlands", "🇳🇱", false),
            Language("pl", "Polish", "Polski", "🇵🇱", false),
            Language("id", "Indonesian", "Bahasa Indonesia", "🇮🇩", false)
        )

        fun findByCode(code: String): Language {
            return ALL_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
                ?: Language(code, code.uppercase(), code.uppercase(), "🌐")
        }
    }
}

enum class ActiveScreen {
    HOME,
    LIVE_TRANSLATE,
    CONVERSATION,
    HISTORY,
    SAVED,
    LANGUAGES,
    PROFILE,
    SETTINGS
}
