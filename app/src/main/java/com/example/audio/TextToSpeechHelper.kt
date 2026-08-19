package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    var isSpeaking = false
        private set

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                }
            })
        } else {
            Log.e("TextToSpeechHelper", "TTS Initialization failed with status $status")
        }
    }

    fun setSpeechRate(rate: Float) {
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    fun setPitch(pitch: Float) {
        if (isInitialized) {
            tts?.setPitch(pitch)
        }
    }

    fun speak(text: String, locale: Locale, speedRate: Float = 1.0f, isFemaleVoice: Boolean = true, onStart: (() -> Unit)? = null, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) return

        try {
            tts?.setSpeechRate(speedRate)
            tts?.setPitch(if (isFemaleVoice) 1.1f else 0.85f)

            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if exact locale unavailable
                tts?.language = Locale.US
            }

            val utteranceId = "utterance_${System.currentTimeMillis()}"
            onStart?.invoke()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "Error speaking text", e)
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            isSpeaking = false
        }
    }

    fun shutdown() {
        if (isInitialized) {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        }
    }
}
