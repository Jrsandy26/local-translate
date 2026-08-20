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
    var continuousMode = false
    private var isPaused = false
    private var currentLocale: Locale = Locale.getDefault()
    private var restartRunnable: Runnable? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(locale: Locale = Locale.getDefault()) {
        currentLocale = locale
        isPaused = false
        cancelRestart()
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
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error starting speech recognizer", e)
                isListening = false
                this@SpeechRecognitionHelper.onListeningStateChanged(false)
            }
        }
    }

    fun pauseListening() {
        isPaused = true
        cancelRestart()
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error pausing speech recognizer", e)
            }
            isListening = false
            this@SpeechRecognitionHelper.onListeningStateChanged(false)
        }
    }

    fun resumeListening(locale: Locale = currentLocale) {
        if (!isPaused && isListening) return
        isPaused = false
        continuousMode = true
        startListening(locale)
    }

    fun stopListening() {
        continuousMode = false
        isPaused = false
        cancelRestart()
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error stopping listening", e)
            }
            isListening = false
            this@SpeechRecognitionHelper.onListeningStateChanged(false)
        }
    }

    fun destroy() {
        continuousMode = false
        isPaused = false
        cancelRestart()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error destroying speech recognizer", e)
            }
            isListening = false
            this@SpeechRecognitionHelper.onListeningStateChanged(false)
        }
    }

    private fun cancelRestart() {
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = null
    }

    private fun scheduleRestart(delayMs: Long = 400) {
        if (!continuousMode || isPaused) return
        cancelRestart()
        restartRunnable = Runnable {
            if (continuousMode && !isPaused) {
                try {
                    speechRecognizer?.cancel()
                    startListening(currentLocale)
                } catch (e: Throwable) {
                    Log.e("SpeechRecognition", "Error restarting recognizer", e)
                }
            }
        }
        mainHandler.postDelayed(restartRunnable!!, delayMs)
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                this@SpeechRecognitionHelper.onListeningStateChanged.invoke(true)
            }

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
                Log.d("SpeechRecognition", "Recognition code: $error")
                // Only notify inactive state if not in continuous mode or if paused
                if (!continuousMode || isPaused) {
                    this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                    isListening = false
                }
                if (continuousMode && !isPaused) {
                    // For speech timeouts or client busy, restart with safe debounce
                    val delay = when (error) {
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 800L
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT, SpeechRecognizer.ERROR_NO_MATCH -> 400L
                        else -> 600L
                    }
                    // Recreate recognizer if client became corrupted
                    if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        try {
                            speechRecognizer?.destroy()
                            speechRecognizer = null
                        } catch (_: Throwable) {}
                    }
                    scheduleRestart(delay)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val finalResult = matches?.firstOrNull() ?: ""
                if (finalResult.isNotBlank()) {
                    this@SpeechRecognitionHelper.onFinalSpeechResult.invoke(finalResult)
                }
                if (!continuousMode || isPaused) {
                    this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                    isListening = false
                } else {
                    scheduleRestart(300)
                }
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
