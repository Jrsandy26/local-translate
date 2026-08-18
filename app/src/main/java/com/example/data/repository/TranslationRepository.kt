package com.example.data.repository

import com.example.data.db.TranslationDao
import com.example.model.SessionWithSegments
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class TranslationRepository(private val dao: TranslationDao) {

    val allSessions: Flow<List<SessionWithSegments>> = dao.getAllSessionsWithSegments()

    fun getSession(sessionId: Long): Flow<SessionWithSegments?> = dao.getSessionWithSegments(sessionId)

    suspend fun checkAndSeedInitialData() {
        withContext(Dispatchers.IO) {
            val count = dao.getSessionCount()
            if (count == 0) {
                // Seed initial "Live translation 14" matching reference
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2026)
                    set(Calendar.MONTH, Calendar.JULY)
                    set(Calendar.DAY_OF_MONTH, 14)
                    set(Calendar.HOUR_OF_DAY, 16)
                    set(Calendar.MINUTE, 29)
                    set(Calendar.SECOND, 0)
                }

                val session14 = TranslationSession(
                    title = "Live translation 14",
                    timestamp = cal.timeInMillis,
                    durationSeconds = 2118, // 35:18
                    sourceLanguageCode = "en",
                    targetLanguageCode = "ja",
                    isFavorite = true
                )
                val sessionId14 = dao.insertSession(session14)

                val segments14 = listOf(
                    TranscriptSegment(
                        sessionId = sessionId14,
                        timeOffsetSeconds = 5,
                        sourceText = "I would like to express my heartfelt gratitude for your presentations and your time today.",
                        translatedText = "本日は、ご発表と貴重なお時間をいただき、心より感謝申し上げます。",
                        speaker = "Speaker 1",
                        orderIndex = 0
                    ),
                    TranscriptSegment(
                        sessionId = sessionId14,
                        timeOffsetSeconds = 25,
                        sourceText = "When I saw your activity book, I had a good impression. I thought that you were pursuing your activities well and all that. But when I came here I am very sorry to say that I was very disappointed because the results were also not like how I was expected.",
                        translatedText = "活動報告書を見たときは、良い印象を受けました。活動をしっかりやっているな、と思っていました。しかし、ここに来て、結果も期待していたようなものではなかったので、大変申し訳ありませんが、非常にがっかりしました。",
                        speaker = "Speaker 1",
                        orderIndex = 1
                    ),
                    TranscriptSegment(
                        sessionId = sessionId14,
                        timeOffsetSeconds = 40,
                        sourceText = "The presentation here was quite different from the activity book and I was disappointed with how we were progressing with it.",
                        translatedText = "ここでのプレゼンテーションは、アクティビティブックとはかなり異なっていて、その進め方にがっかりしました。",
                        speaker = "Speaker 1",
                        orderIndex = 2
                    )
                )
                dao.insertSegments(segments14)

                // Add another sample session for demonstration: "Business Strategy Sync"
                val session15 = TranslationSession(
                    title = "Live translation 15 (Strategy)",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    durationSeconds = 645, // 10:45
                    sourceLanguageCode = "en",
                    targetLanguageCode = "es",
                    isFavorite = false
                )
                val sessionId15 = dao.insertSession(session15)
                val segments15 = listOf(
                    TranscriptSegment(
                        sessionId = sessionId15,
                        timeOffsetSeconds = 12,
                        sourceText = "Good morning everyone, thank you for joining us.",
                        translatedText = "Buenos días a todos, gracias por acompañarnos.",
                        speaker = "Speaker 1",
                        orderIndex = 0
                    ),
                    TranscriptSegment(
                        sessionId = sessionId15,
                        timeOffsetSeconds = 34,
                        sourceText = "We are pleased to introduce our latest project results.",
                        translatedText = "Nos complace presentar los últimos resultados de nuestro proyecto.",
                        speaker = "Speaker 2",
                        orderIndex = 1
                    ),
                    TranscriptSegment(
                        sessionId = sessionId15,
                        timeOffsetSeconds = 68,
                        sourceText = "Are there any questions or comments regarding this proposal?",
                        translatedText = "¿Hay alguna pregunta o comentario sobre esta propuesta?",
                        speaker = "Speaker 1",
                        orderIndex = 2
                    )
                )
                dao.insertSegments(segments15)
            }
        }
    }

    suspend fun createNewSession(
        title: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): Long {
        return withContext(Dispatchers.IO) {
            val session = TranslationSession(
                title = title,
                timestamp = System.currentTimeMillis(),
                durationSeconds = 0,
                sourceLanguageCode = sourceLanguageCode,
                targetLanguageCode = targetLanguageCode
            )
            dao.insertSession(session)
        }
    }

    suspend fun addSegment(
        sessionId: Long,
        timeOffsetSeconds: Int,
        sourceText: String,
        translatedText: String,
        speaker: String,
        orderIndex: Int
    ): Long {
        return withContext(Dispatchers.IO) {
            val segment = TranscriptSegment(
                sessionId = sessionId,
                timeOffsetSeconds = timeOffsetSeconds,
                sourceText = sourceText,
                translatedText = translatedText,
                speaker = speaker,
                orderIndex = orderIndex
            )
            dao.insertSegment(segment)
        }
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        withContext(Dispatchers.IO) {
            dao.updateSessionTitle(sessionId, title)
        }
    }

    suspend fun updateSessionDuration(sessionId: Long, durationSeconds: Int) {
        withContext(Dispatchers.IO) {
            dao.updateDuration(sessionId, durationSeconds)
        }
    }

    suspend fun toggleFavorite(sessionId: Long, currentStatus: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateFavoriteStatus(sessionId, !currentStatus)
        }
    }

    suspend fun deleteSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteSession(sessionId)
        }
    }

    suspend fun getSessionOnce(sessionId: Long): SessionWithSegments? {
        return withContext(Dispatchers.IO) {
            dao.getSessionWithSegmentsOnce(sessionId)
        }
    }
}
