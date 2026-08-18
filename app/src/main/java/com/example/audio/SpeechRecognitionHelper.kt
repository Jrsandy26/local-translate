package com.example.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

class SpeechRecognitionHelper(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening = false
        private set
    private var continuousMode = true
    private var currentLanguageLocale: Locale = Locale.US
    private var consecutiveErrors = 0
    private val maxConsecutiveErrors = 2

    fun isAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (e: Exception) {
            false
        }
    }

    private fun hasAudioPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun startListening(locale: Locale, continuous: Boolean = true) {
        mainHandler.post {
            if (isListening) return@post
            this.currentLanguageLocale = locale
            this.continuousMode = continuous

            if (!hasAudioPermission()) {
                Log.w("SpeechRecognition", "Audio permission not granted")
                isListening = false
                onListeningStateChanged(false)
                return@post
            }

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = createRecognizerInstance()
                    if (speechRecognizer == null) {
                        Log.w("SpeechRecognition", "SpeechRecognizer instance could not be created")
                        isListening = false
                        onListeningStateChanged(false)
                        return@post
                    }
                    speechRecognizer?.setRecognitionListener(createListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    }
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                onListeningStateChanged(true)
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error starting listening", e)
                isListening = false
                onListeningStateChanged(false)
                cleanUpRecognizer()
            }
        }
    }

    private fun createRecognizerInstance(): SpeechRecognizer? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                try {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } catch (e: Throwable) {
                    if (SpeechRecognizer.isRecognitionAvailable(context)) {
                        SpeechRecognizer.createSpeechRecognizer(context)
                    } else null
                }
            } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.e("SpeechRecognition", "Failed to create SpeechRecognizer", e)
            null
        }
    }

    fun stopListening() {
        mainHandler.post {
            consecutiveErrors = 0
            if (!isListening) return@post
            try {
                speechRecognizer?.stopListening()
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error stopping listening", e)
            }
            isListening = false
            onListeningStateChanged(false)
        }
    }

    private fun cleanUpRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Throwable) {
            Log.e("SpeechRecognition", "Error cleaning up recognizer", e)
        } finally {
            speechRecognizer = null
            isListening = false
        }
    }

    fun destroy() {
        mainHandler.post {
            consecutiveErrors = 0
            cleanUpRecognizer()
            onListeningStateChanged(false)
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            consecutiveErrors = 0
            onListeningStateChanged(true)
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            try {
                onRmsChanged(rmsdB)
            } catch (e: Throwable) {
                // Ignore visualizer errors
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            Log.d("SpeechRecognition", "Speech error code: $error")
            consecutiveErrors++

            // Check if error is fatal or client side
            val isFatal = error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                    error == SpeechRecognizer.ERROR_CLIENT ||
                    consecutiveErrors >= maxConsecutiveErrors

            if (continuousMode && isListening && !isFatal) {
                mainHandler.postDelayed({
                    if (isListening) {
                        try {
                            speechRecognizer?.cancel()
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageLocale.toLanguageTag())
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageLocale.toLanguageTag())
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                            }
                            speechRecognizer?.startListening(intent)
                        } catch (e: Throwable) {
                            Log.e("SpeechRecognition", "Failed restarting recognizer", e)
                            isListening = false
                            onListeningStateChanged(false)
                        }
                    }
                }, 1200)
            } else {
                isListening = false
                onListeningStateChanged(false)
            }
        }

        override fun onResults(results: Bundle?) {
            consecutiveErrors = 0
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: ""
            if (recognizedText.isNotBlank()) {
                try {
                    onFinalResult(recognizedText)
                } catch (e: Throwable) {
                    Log.e("SpeechRecognition", "Error in onFinalResult", e)
                }
            }

            if (continuousMode && isListening) {
                mainHandler.postDelayed({
                    if (isListening) {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageLocale.toLanguageTag())
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageLocale.toLanguageTag())
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                            }
                            speechRecognizer?.startListening(intent)
                        } catch (e: Throwable) {
                            isListening = false
                            onListeningStateChanged(false)
                        }
                    }
                }, 400)
            } else {
                isListening = false
                onListeningStateChanged(false)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull() ?: ""
            if (partialText.isNotBlank()) {
                try {
                    onPartialResult(partialText)
                } catch (e: Throwable) {
                    Log.e("SpeechRecognition", "Error in onPartialResult", e)
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}


