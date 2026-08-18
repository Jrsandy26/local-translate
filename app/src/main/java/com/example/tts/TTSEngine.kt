package com.example.tts

import java.io.File

interface TTSEngine {
    fun load(modelDirectory: File): Boolean
    fun synthesize(text: String): AudioResult
    fun unload()
}

data class AudioResult(
    val pcmData: ShortArray,
    val sampleRate: Int,
    val text: String,
    val phonemizationTimeMs: Long,
    val onnxInferenceTimeMs: Long,
    val totalTimeMs: Long,
    val audioDurationMs: Long,
    val rtf: Float,
    val peakMemoryMb: Float
)
