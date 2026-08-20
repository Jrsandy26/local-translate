package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var pitch: Float = 1.0f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(pitch)
            } else {
                Log.e("TTSHelper", "TTS Initialization failed")
            }
        }
    }

    fun speak(text: String, langCode: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        if (!isInitialized || tts == null || text.isBlank()) return

        val locale = when (langCode.lowercase()) {
            "ja" -> Locale.JAPANESE
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "zh" -> Locale.CHINESE
            "ko" -> Locale.KOREAN
            "it" -> Locale.ITALIAN
            "pt" -> Locale("pt", "BR")
            "ru" -> Locale("ru", "RU")
            "hi" -> Locale("hi", "IN")
            "ar" -> Locale("ar", "SA")
            else -> Locale.ENGLISH
        }

        tts?.language = locale
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onStart()
            }

            override fun onDone(utteranceId: String?) {
                onDone()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDone()
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
