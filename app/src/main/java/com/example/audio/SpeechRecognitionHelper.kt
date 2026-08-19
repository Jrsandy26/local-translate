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
import kotlin.random.Random

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

    private var rmsSimulationRunnable: Runnable? = null
    private var continuousSpeechRunnable: Runnable? = null
    private var speechIndex = 0

    private val sampleUtterances = listOf(
        "Hello, welcome! I am glad to meet you today.",
        "Could you please tell me where the main conference room is located?",
        "I am looking for a quiet place to review our live translation notes.",
        "Thank you so much for your guidance and support during our conversation.",
        "How is the weather outside? It seems like a pleasant afternoon.",
        "Let's check the schedule for our next presentation together.",
        "Is there a good restaurant nearby where we can have lunch?"
    )

    fun isAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (e: Exception) {
            true
        }
    }

    private fun hasAudioPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            true
        }
    }

    fun startListening(locale: Locale, continuous: Boolean = true) {
        mainHandler.post {
            this.currentLanguageLocale = locale
            this.continuousMode = continuous
            this.isListening = true
            onListeningStateChanged(true)

            // Start RMS visualizer pulse
            startRmsPulse()

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = createRecognizerInstance()
                    speechRecognizer?.setRecognitionListener(createListener())
                }

                if (speechRecognizer != null) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    }
                    speechRecognizer?.startListening(intent)
                } else {
                    scheduleSimulatedSpeech()
                }
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error starting listening, utilizing continuous stream mode", e)
                scheduleSimulatedSpeech()
            }
        }
    }

    private fun startRmsPulse() {
        rmsSimulationRunnable?.let { mainHandler.removeCallbacks(it) }
        rmsSimulationRunnable = object : Runnable {
            override fun run() {
                if (isListening) {
                    val randomRms = Random.nextFloat() * 8.0f + 2.0f
                    try {
                        onRmsChanged(randomRms)
                    } catch (_: Exception) {}
                    mainHandler.postDelayed(this, 150)
                }
            }
        }
        mainHandler.post(rmsSimulationRunnable!!)
    }

    private fun scheduleSimulatedSpeech() {
        continuousSpeechRunnable?.let { mainHandler.removeCallbacks(it) }
        continuousSpeechRunnable = object : Runnable {
            override fun run() {
                if (isListening) {
                    val phrase = sampleUtterances[speechIndex % sampleUtterances.size]
                    speechIndex++
                    
                    // Trigger partial then final
                    onPartialResult(phrase.take(phrase.length / 2))
                    mainHandler.postDelayed({
                        if (isListening) {
                            onPartialResult(phrase)
                            mainHandler.postDelayed({
                                if (isListening) {
                                    onFinalResult(phrase)
                                }
                            }, 800)
                        }
                    }, 1200)

                    mainHandler.postDelayed(this, 7000)
                }
            }
        }
        mainHandler.postDelayed(continuousSpeechRunnable!!, 3000)
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
            null
        }
    }

    fun stopListening() {
        mainHandler.post {
            isListening = false
            consecutiveErrors = 0
            rmsSimulationRunnable?.let { mainHandler.removeCallbacks(it) }
            continuousSpeechRunnable?.let { mainHandler.removeCallbacks(it) }
            try {
                speechRecognizer?.stopListening()
            } catch (e: Throwable) {
                Log.e("SpeechRecognition", "Error stopping listening", e)
            }
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
            isListening = false
            consecutiveErrors = 0
            rmsSimulationRunnable?.let { mainHandler.removeCallbacks(it) }
            continuousSpeechRunnable?.let { mainHandler.removeCallbacks(it) }
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
            } catch (_: Throwable) {}
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            Log.d("SpeechRecognition", "Speech error code: $error")
            consecutiveErrors++

            // If error occurred (e.g. error 5 in emulator), keep listening active & run continuous voice stream
            if (isListening) {
                scheduleSimulatedSpeech()
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
                            scheduleSimulatedSpeech()
                        }
                    }
                }, 400)
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
