package com.example.data.repository

import com.example.data.db.TranslationDao
import com.example.model.RecentTranslation
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val dao: TranslationDao) {
    val recentTranslations: Flow<List<RecentTranslation>> = dao.getAllRecentTranslations()
    val favoriteTranslations: Flow<List<RecentTranslation>> = dao.getFavoriteTranslations()
    val sessions: Flow<List<TranslationSession>> = dao.getAllSessions()

    suspend fun addRecentTranslation(source: String, translated: String, sourceLang: String, targetLang: String) {
        val item = RecentTranslation(
            sourceText = source,
            translatedText = translated,
            sourceLangCode = sourceLang,
            targetLangCode = targetLang
        )
        dao.insertRecentTranslation(item)
    }

    suspend fun toggleFavorite(item: RecentTranslation) {
        dao.updateRecentTranslation(item.copy(isFavorite = !item.isFavorite))
    }

    suspend fun deleteRecent(item: RecentTranslation) {
        dao.deleteRecentTranslation(item)
    }

    suspend fun clearHistory() {
        dao.clearAllRecentTranslations()
    }

    suspend fun createSession(title: String, src: String, tgt: String): Long {
        val session = TranslationSession(
            title = title,
            sourceLanguageCode = src,
            targetLanguageCode = tgt
        )
        return dao.insertSession(session)
    }

    suspend fun saveCompletedSession(
        title: String,
        src: String,
        tgt: String,
        durationSeconds: Int,
        audioPath: String?,
        segments: List<TranscriptSegment>
    ): Long {
        val session = TranslationSession(
            title = title,
            sourceLanguageCode = src,
            targetLanguageCode = tgt,
            durationSeconds = durationSeconds,
            audioFilePath = audioPath
        )
        val sessionId = dao.insertSession(session)
        for (seg in segments) {
            dao.insertSegment(seg.copy(sessionId = sessionId))
        }
        return sessionId
    }

    suspend fun deleteSession(session: TranslationSession) {
        dao.deleteSegmentsForSession(session.id)
        dao.deleteSession(session)
    }

    suspend fun clearAllSessions() {
        dao.clearAllSessions()
    }

    fun getSegments(sessionId: Long): Flow<List<TranscriptSegment>> {
        return dao.getSegmentsForSession(sessionId)
    }

    suspend fun getSegmentsList(sessionId: Long): List<TranscriptSegment> {
        return dao.getSegmentsListForSession(sessionId)
    }

    suspend fun addSegment(sessionId: Long, speaker: String, srcText: String, tgtText: String, isSrc: Boolean) {
        val seg = TranscriptSegment(
            sessionId = sessionId,
            speaker = speaker,
            sourceText = srcText,
            translatedText = tgtText,
            isSourceSpeaker = isSrc
        )
        dao.insertSegment(seg)
    }

    suspend fun seedInitialDataIfEmpty() {
        // We will seed the initial item displayed in the mock if database is fresh
    }
}
