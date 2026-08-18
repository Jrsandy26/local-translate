package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_sessions")
data class TranslationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val sourceLanguageCode: String = "en",
    val targetLanguageCode: String = "ja",
    val isFavorite: Boolean = false,
    val audioFilePath: String? = null
)
