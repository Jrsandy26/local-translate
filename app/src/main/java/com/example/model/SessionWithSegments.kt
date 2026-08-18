package com.example.model

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithSegments(
    @Embedded
    val session: TranslationSession,

    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val segments: List<TranscriptSegment>
) {
    fun getSortedSegments(): List<TranscriptSegment> {
        return segments.sortedBy { it.orderIndex }
    }
}
