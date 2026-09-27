package com.rivatranslate.audio

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
    private val onErrorOccurred: ((Int, String) -> Unit)? = null,
    private val onAudioBufferReceived: ((ByteArray?) -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    var continuousMode = false
    private var isPaused = false
    private var currentLocale: Locale = Locale.getDefault()
    private var restartRunnable: Runnable? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
    private var isSystemMuted = false

    private fun muteSystemSounds() {
        if (isSystemMuted) return
        try {
            isSystemMuted = true
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                audioManager?.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_MUTE, 0)
                audioManager?.adjustStreamVolume(android.media.AudioManager.STREAM_SYSTEM, android.media.AudioManager.ADJUST_MUTE, 0)
                audioManager?.adjustStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, android.media.AudioManager.ADJUST_MUTE, 0)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(android.media.AudioManager.STREAM_MUSIC, true)
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(android.media.AudioManager.STREAM_SYSTEM, true)
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(android.media.AudioManager.STREAM_NOTIFICATION, true)
            }
        } catch (e: Throwable) {
            Log.w("SpeechRecognition", "Cannot mute system stream", e)
        }
    }

    private fun restoreSystemSounds() {
        if (!isSystemMuted) return
        try {
            isSystemMuted = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                audioManager?.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_UNMUTE, 0)
                audioManager?.adjustStreamVolume(android.media.AudioManager.STREAM_SYSTEM, android.media.AudioManager.ADJUST_UNMUTE, 0)
                audioManager?.adjustStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, android.media.AudioManager.ADJUST_UNMUTE, 0)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(android.media.AudioManager.STREAM_MUSIC, false)
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(android.media.AudioManager.STREAM_SYSTEM, false)
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(android.media.AudioManager.STREAM_NOTIFICATION, false)
            }
        } catch (e: Throwable) {
            Log.w("SpeechRecognition", "Cannot restore system stream", e)
        }
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(locale: Locale = Locale.getDefault()) {
        currentLocale = locale
        isPaused = false
        cancelRestart()
        mainHandler.post {
            try {
                muteSystemSounds()
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val langTag = locale.toLanguageTag()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(langTag))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                this@SpeechRecognitionHelper.onListeningStateChanged(true)
            } catch (e: Throwable) {
                restoreSystemSounds()
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
            restoreSystemSounds()
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
            restoreSystemSounds()
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
            restoreSystemSounds()
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
                if (!continuousMode) {
                    mainHandler.postDelayed({ restoreSystemSounds() }, 400)
                }
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
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                    SpeechRecognizer.ERROR_SERVER -> "Error from server"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                Log.d("SpeechRecognition", "Recognition error: $message ($error)")
                this@SpeechRecognitionHelper.onErrorOccurred?.invoke(error, message)
                
                // Only notify inactive state if not in continuous mode or if paused
                if (!continuousMode || isPaused) {
                    this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                    isListening = false
                }
                
                if (continuousMode && !isPaused) {
                    val shouldRestart = when (error) {
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                        SpeechRecognizer.ERROR_CLIENT,
                        SpeechRecognizer.ERROR_AUDIO,
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        SpeechRecognizer.ERROR_SERVER -> true
                        else -> false // e.g. permissions (9), language unavailable (13), cannot check support (14)
                    }

                    if (shouldRestart) {
                        // For speech timeouts, client busy, or audio errors, restart with safe debounce
                        val delay = when (error) {
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 1000L
                            SpeechRecognizer.ERROR_AUDIO -> 1500L // Give some time for mic to be released
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT, SpeechRecognizer.ERROR_NO_MATCH -> 400L
                            else -> 800L
                        }
                        
                        // Recreate recognizer if client became corrupted or busy
                        if (error == SpeechRecognizer.ERROR_CLIENT || 
                            error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                            error == SpeechRecognizer.ERROR_AUDIO) {
                            try {
                                speechRecognizer?.destroy()
                                speechRecognizer = null
                            } catch (_: Throwable) {}
                        }
                        scheduleRestart(delay)
                    } else {
                        restoreSystemSounds()
                        isListening = false
                        continuousMode = false
                        this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                    }
                } else {
                    restoreSystemSounds()
                    isListening = false
                    this@SpeechRecognitionHelper.onListeningStateChanged.invoke(false)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val finalResult = matches?.firstOrNull() ?: ""
                if (finalResult.isNotBlank()) {
                    this@SpeechRecognitionHelper.onFinalSpeechResult.invoke(finalResult)
                }
                if (!continuousMode || isPaused) {
                    restoreSystemSounds()
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
