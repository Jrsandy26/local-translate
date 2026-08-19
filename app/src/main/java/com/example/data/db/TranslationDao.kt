package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.SessionWithSegments
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Transaction
    @Query("SELECT * FROM translation_sessions ORDER BY timestamp DESC")
    fun getAllSessionsWithSegments(): Flow<List<SessionWithSegments>>

    @Transaction
    @Query("SELECT * FROM translation_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionWithSegments(sessionId: Long): Flow<SessionWithSegments?>

    @Transaction
    @Query("SELECT * FROM translation_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionWithSegmentsOnce(sessionId: Long): SessionWithSegments?

    @Query("SELECT COUNT(*) FROM translation_sessions")
    suspend fun getSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TranslationSession): Long

    @Update
    suspend fun updateSession(session: TranslationSession)

    @Query("UPDATE translation_sessions SET title = :title WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: Long, title: String)

    @Query("UPDATE translation_sessions SET isFavorite = :isFav WHERE id = :sessionId")
    suspend fun updateFavoriteStatus(sessionId: Long, isFav: Boolean)

    @Query("UPDATE translation_sessions SET durationSeconds = :duration WHERE id = :sessionId")
    suspend fun updateDuration(sessionId: Long, duration: Int)

    @Query("UPDATE translation_sessions SET audioFilePath = :audioPath WHERE id = :sessionId")
    suspend fun updateAudioFilePath(sessionId: Long, audioPath: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: TranscriptSegment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptSegment>)

    @Query("DELETE FROM transcript_segments WHERE sessionId = :sessionId")
    suspend fun deleteSegmentsForSession(sessionId: Long)

    @Query("DELETE FROM translation_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM transcript_segments WHERE id = :segmentId")
    suspend fun deleteSegment(segmentId: Long)
}
