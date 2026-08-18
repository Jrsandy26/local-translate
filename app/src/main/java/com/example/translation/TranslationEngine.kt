package com.example.translation

import java.io.File

interface TranslationEngine {
    fun load(modelDirectory: File): Boolean
    fun translate(text: String, sourceLanguage: String = "ja", targetLanguage: String = "en"): TranslationResult
    fun unload()
}

data class TranslationResult(
    val translatedText: String,
    val inputTokenCount: Int,
    val outputTokenCount: Int,
    val encoderTimeMs: Long,
    val decoderTimeMs: Long,
    val totalTimeMs: Long,
    val peakMemoryKb: Long
)
