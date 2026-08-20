package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SpeechRecognitionHelper(
    private val context: Context,
    private val onPartialSpeechResult: (String) -> Unit,
    private val onFinalSpeechResult: (String) -> Unit,
    private val onRmsLevelChanged: (Float) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onAudioBufferReceived: ((ByteArray?) -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(locale: Locale = Locale.getDefault()) {
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                this@SpeechRecognitionHelper.onListeningStateChanged(true)
            } catch (e: Exception) {
                Log.e("SpeechRecognition", "Error starting speech recognizer", e)
                this@SpeechRecognitionHelper.onListeningStateChanged(false)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("SpeechRecognition", "Error stopping listening", e)
            }
            isListening = false
            this@SpeechRecognitionHelper.onListeningStateChanged(false)
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e("SpeechRecognition", "Error destroying speech recognizer", e)
            }
            isListening = false
            this@SpeechRecognitionHelper.onListeningStateChanged(false)
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1f)
                this@SpeechRecognitionHelper.onRmsLevelChanged.invoke(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                try {
                    this@SpeechRecognitionHelper.onAudioBufferReceived?.invoke(buffer)
                } catch (_: Throwable) {}
            }

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Log.w("SpeechRecognition", "Recognition error: $error")
                this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val finalResult = matches?.firstOrNull() ?: ""
                if (finalResult.isNotBlank()) {
                    this@SpeechRecognitionHelper.onFinalSpeechResult.invoke(finalResult)
                }
                this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull() ?: ""
                if (partialText.isNotBlank()) {
                    this@SpeechRecognitionHelper.onPartialSpeechResult.invoke(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
