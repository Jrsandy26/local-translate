package com.rivatranslate.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_translations")
data class RecentTranslation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLangCode: String,
    val targetLangCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "translation_sessions")
data class TranslationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isFavorite: Boolean = false,
    val audioFilePath: String? = null
)

@Entity(tableName = "transcript_segments")
data class TranscriptSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val speaker: String = "Speaker 1",
    val sourceText: String,
    val translatedText: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isSourceSpeaker: Boolean = true
)
