package com.example.data.db

import androidx.room.*
import com.example.model.RecentTranslation
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM recent_translations ORDER BY timestamp DESC")
    fun getAllRecentTranslations(): Flow<List<RecentTranslation>>

    @Query("SELECT * FROM recent_translations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteTranslations(): Flow<List<RecentTranslation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentTranslation(item: RecentTranslation): Long

    @Update
    suspend fun updateRecentTranslation(item: RecentTranslation)

    @Delete
    suspend fun deleteRecentTranslation(item: RecentTranslation)

    @Query("DELETE FROM recent_translations WHERE id = :id")
    suspend fun deleteRecentTranslationById(id: Long)

    @Query("DELETE FROM recent_translations")
    suspend fun clearAllRecentTranslations()

    // Sessions & Segments
    @Query("SELECT * FROM translation_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<TranslationSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TranslationSession): Long

    @Update
    suspend fun updateSession(session: TranslationSession)

    @Delete
    suspend fun deleteSession(session: TranslationSession)

    @Query("DELETE FROM translation_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM transcript_segments WHERE sessionId = :sessionId")
    suspend fun deleteSegmentsForSession(sessionId: Long)

    @Query("DELETE FROM translation_sessions")
    suspend fun clearAllSessions()

    @Query("SELECT * FROM transcript_segments WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    fun getSegmentsForSession(sessionId: Long): Flow<List<TranscriptSegment>>

    @Query("SELECT * FROM transcript_segments WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun getSegmentsListForSession(sessionId: Long): List<TranscriptSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: TranscriptSegment): Long
}
