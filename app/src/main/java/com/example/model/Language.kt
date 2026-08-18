package com.example.model

import java.util.Locale

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String,
    val locale: Locale,
    val modelSizeMb: Int = 45,
    val isDownloaded: Boolean = true
) {
    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            Language(
                code = "en",
                name = "English",
                nativeName = "English",
                flag = "🇺🇸",
                locale = Locale.US,
                modelSizeMb = 38,
                isDownloaded = true
            ),
            Language(
                code = "ja",
                name = "Japanese",
                nativeName = "日本語",
                flag = "🇯🇵",
                locale = Locale.JAPAN,
                modelSizeMb = 48,
                isDownloaded = true
            ),
            Language(
                code = "es",
                name = "Spanish",
                nativeName = "Español",
                flag = "🇪🇸",
                locale = Locale("es", "ES"),
                modelSizeMb = 42,
                isDownloaded = true
            ),
            Language(
                code = "fr",
                name = "French",
                nativeName = "Français",
                flag = "🇫🇷",
                locale = Locale.FRANCE,
                modelSizeMb = 40,
                isDownloaded = true
            ),
            Language(
                code = "de",
                name = "German",
                nativeName = "Deutsch",
                flag = "🇩🇪",
                locale = Locale.GERMANY,
                modelSizeMb = 44,
                isDownloaded = true
            ),
            Language(
                code = "zh",
                name = "Chinese",
                nativeName = "中文",
                flag = "🇨🇳",
                locale = Locale.SIMPLIFIED_CHINESE,
                modelSizeMb = 52,
                isDownloaded = true
            ),
            Language(
                code = "hi",
                name = "Hindi",
                nativeName = "हिन्दी",
                flag = "🇮🇳",
                locale = Locale("hi", "IN"),
                modelSizeMb = 46,
                isDownloaded = true
            ),
            Language(
                code = "ko",
                name = "Korean",
                nativeName = "한국어",
                flag = "🇰🇷",
                locale = Locale.KOREA,
                modelSizeMb = 45,
                isDownloaded = true
            ),
            Language(
                code = "it",
                name = "Italian",
                nativeName = "Italiano",
                flag = "🇮🇹",
                locale = Locale.ITALY,
                modelSizeMb = 39,
                isDownloaded = true
            ),
            Language(
                code = "pt",
                name = "Portuguese",
                nativeName = "Português",
                flag = "🇧🇷",
                locale = Locale("pt", "BR"),
                modelSizeMb = 41,
                isDownloaded = true
            ),
            Language(
                code = "ru",
                name = "Russian",
                nativeName = "Русский",
                flag = "🇷🇺",
                locale = Locale("ru", "RU"),
                modelSizeMb = 47,
                isDownloaded = true
            ),
            Language(
                code = "ar",
                name = "Arabic",
                nativeName = "العربية",
                flag = "🇸🇦",
                locale = Locale("ar", "SA"),
                modelSizeMb = 50,
                isDownloaded = true
            ),
            Language(
                code = "vi",
                name = "Vietnamese",
                nativeName = "Tiếng Việt",
                flag = "🇻🇳",
                locale = Locale("vi", "VN"),
                modelSizeMb = 43,
                isDownloaded = true
            )
        )

        fun findByCode(code: String): Language {
            return SUPPORTED_LANGUAGES.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: SUPPORTED_LANGUAGES[0]
        }
    }
}
