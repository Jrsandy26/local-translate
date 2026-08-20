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

    var preferredVoiceGender: String = "Default"

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

        // Filter and choose preferred voice gender if not "Default"
        if (preferredVoiceGender != "Default") {
            try {
                val availableVoices = tts?.voices
                if (availableVoices != null) {
                    val matchingVoices = availableVoices.filter { voice ->
                        voice.locale.language == locale.language && 
                        (locale.country.isEmpty() || voice.locale.country == locale.country)
                    }

                    if (matchingVoices.isNotEmpty()) {
                        val isMaleRequested = preferredVoiceGender.equals("Male", ignoreCase = true)
                        val selectedVoice = matchingVoices.find { voice ->
                            val nameLower = voice.name.lowercase()
                            if (isMaleRequested) {
                                nameLower.contains("male") || 
                                nameLower.contains("-b-") || 
                                nameLower.contains("-e-") || 
                                nameLower.contains("-f-") || 
                                nameLower.contains("-iol-") || 
                                nameLower.contains("-iom-") || 
                                nameLower.contains("-rgf-")
                            } else {
                                nameLower.contains("female") || 
                                nameLower.contains("-a-") || 
                                nameLower.contains("-c-") || 
                                nameLower.contains("-d-") || 
                                nameLower.contains("-g-") || 
                                nameLower.contains("-sfg-") || 
                                nameLower.contains("-tpf-")
                            }
                        } ?: matchingVoices.firstOrNull()

                        if (selectedVoice != null) {
                            tts?.voice = selectedVoice
                            Log.d("TTSHelper", "Selected voice: ${selectedVoice.name} for gender: $preferredVoiceGender")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TTSHelper", "Error filtering voices: ", e)
            }
        }

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
