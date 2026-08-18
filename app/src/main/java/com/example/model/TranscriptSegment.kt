package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transcript_segments",
    foreignKeys = [
        ForeignKey(
            entity = TranslationSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class TranscriptSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long = 0,
    val timeOffsetSeconds: Int = 0,
    val sourceText: String,
    val translatedText: String,
    val speaker: String = "Speaker 1",
    val orderIndex: Int = 0
) {
    fun getFormattedTimestamp(): String {
        val minutes = timeOffsetSeconds / 60
        val seconds = timeOffsetSeconds % 60
        return String.format("%02d : %02d", minutes, seconds)
    }

    fun getFormattedShortTimestamp(): String {
        val minutes = timeOffsetSeconds / 60
        val seconds = timeOffsetSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
