package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton coordinator between ViewModel, Jetpack Compose UI,
 * and the Android LiveTranslationService Foreground Service.
 */
object LiveSessionManager {

    private val _isSessionRunning = MutableStateFlow(false)
    val isSessionRunning = _isSessionRunning.asStateFlow()

    private val _isSessionPaused = MutableStateFlow(false)
    val isSessionPaused = _isSessionPaused.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds = _timerSeconds.asStateFlow()

    private val _currentRms = MutableStateFlow(0f)
    val currentRms = _currentRms.asStateFlow()

    // Listeners for UI / ViewModel updates
    var onSessionStateChanged: ((running: Boolean, paused: Boolean) -> Unit)? = null
    var onTimerTick: ((seconds: Int) -> Unit)? = null
    var onActionFromNotification: ((action: String) -> Unit)? = null
    var onRmsUpdated: ((rms: Float) -> Unit)? = null

    fun updateRms(rms: Float) {
        _currentRms.value = rms
        onRmsUpdated?.invoke(rms)
    }

    fun updateSessionState(running: Boolean, paused: Boolean) {
        _isSessionRunning.value = running
        _isSessionPaused.value = paused
        onSessionStateChanged?.invoke(running, paused)
    }

    fun updateTimer(seconds: Int) {
        _timerSeconds.value = seconds
        onTimerTick?.invoke(seconds)
    }

    fun handleNotificationAction(action: String) {
        onActionFromNotification?.invoke(action)
    }

    fun startService(context: Context) {
        try {
            val intent = Intent(context, LiveTranslationService::class.java).apply {
                action = LiveTranslationService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Throwable) {
            android.util.Log.w("LiveSessionManager", "Unable to start foreground service: ${e.message}")
        }
        updateSessionState(running = true, paused = false)
    }

    fun pauseService(context: Context) {
        try {
            val intent = Intent(context, LiveTranslationService::class.java).apply {
                action = LiveTranslationService.ACTION_PAUSE
            }
            context.startService(intent)
        } catch (e: Throwable) {
            android.util.Log.w("LiveSessionManager", "Unable to pause service: ${e.message}")
        }
        updateSessionState(running = true, paused = true)
    }

    fun resumeService(context: Context) {
        try {
            val intent = Intent(context, LiveTranslationService::class.java).apply {
                action = LiveTranslationService.ACTION_RESUME
            }
            context.startService(intent)
        } catch (e: Throwable) {
            android.util.Log.w("LiveSessionManager", "Unable to resume service: ${e.message}")
        }
        updateSessionState(running = true, paused = false)
    }

    fun stopService(context: Context) {
        try {
            val intent = Intent(context, LiveTranslationService::class.java).apply {
                action = LiveTranslationService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Throwable) {
            android.util.Log.w("LiveSessionManager", "Unable to stop service: ${e.message}")
        }
        updateSessionState(running = false, paused = false)
    }
}
