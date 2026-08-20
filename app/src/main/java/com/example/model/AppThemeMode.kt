package com.example.model

enum class AppThemeMode(val key: String, val displayName: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}
