package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class LiveTranslationService : Service() {

    companion object {
        const val CHANNEL_ID = "live_translation_channel"
        const val CHANNEL_NAME = "Live Translation"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.service.ACTION_RESUME"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var visualizerJob: Job? = null
    private var secondsElapsed = 0
    private var isPaused = false
    private var currentRms = 0f
    private var isCapturingSound = false
    private var animFrame = 0
    private var lastSoundTimestamp = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Listen for real-time sound RMS levels from speech recognition / audio capture
        LiveSessionManager.onRmsUpdated = { rms ->
            currentRms = rms
            if (rms > 0.8f) {
                isCapturingSound = true
                lastSoundTimestamp = System.currentTimeMillis()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val fromNotif = intent?.getBooleanExtra("FROM_NOTIFICATION", false) ?: false
        when (action) {
            ACTION_START -> {
                secondsElapsed = 0
                isPaused = false
                currentRms = 0f
                isCapturingSound = false
                animFrame = 0
                LiveSessionManager.updateSessionState(running = true, paused = false)
                startForegroundWithNotification()
                startTimer()
                startVisualizerLoop()
            }
            ACTION_PAUSE -> {
                isPaused = true
                isCapturingSound = false
                currentRms = 0f
                LiveSessionManager.updateSessionState(running = true, paused = true)
                if (fromNotif) {
                    LiveSessionManager.handleNotificationAction(ACTION_PAUSE)
                }
                updateNotification()
            }
            ACTION_RESUME -> {
                isPaused = false
                LiveSessionManager.updateSessionState(running = true, paused = false)
                if (fromNotif) {
                    LiveSessionManager.handleNotificationAction(ACTION_RESUME)
                }
                updateNotification()
                startVisualizerLoop()
            }
            ACTION_STOP -> {
                isPaused = false
                isCapturingSound = false
                LiveSessionManager.updateSessionState(running = false, paused = false)
                if (fromNotif) {
                    LiveSessionManager.handleNotificationAction(ACTION_STOP)
                }
                stopForegroundService()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification() {
        try {
            val notification = buildLiveNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } catch (e: Throwable) {
                    try {
                        startForeground(NOTIFICATION_ID, notification)
                    } catch (e2: Throwable) {
                        android.util.Log.e("LiveTranslationService", "Cannot start foreground", e2)
                    }
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            android.util.Log.e("LiveTranslationService", "Error building or starting foreground notification", e)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                delay(1000)
                if (!isPaused) {
                    secondsElapsed++
                    LiveSessionManager.updateTimer(secondsElapsed)
                    updateNotification()
                }
            }
        }
    }

    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = serviceScope.launch {
            var wasCapturing = false
            while (true) {
                delay(120)
                if (isPaused) continue

                val now = System.currentTimeMillis()
                val isCurrentlyCapturing = (now - lastSoundTimestamp < 500) && (currentRms > 0.8f)

                if (isCurrentlyCapturing) {
                    animFrame++
                    isCapturingSound = true
                    wasCapturing = true
                    updateNotification()
                } else if (wasCapturing) {
                    // Sound just stopped: settle visualizer back to idle baseline
                    isCapturingSound = false
                    currentRms = 0f
                    wasCapturing = false
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(NOTIFICATION_ID, buildLiveNotification())
        } catch (e: Throwable) {
            android.util.Log.w("LiveTranslationService", "Cannot update notification: ${e.message}")
        }
    }

    private fun buildLiveNotification(): Notification {
        val customView = RemoteViews(packageName, R.layout.notification_live_translate)

        // Format timer string as MM:SS (e.g., 00:05)
        val minutes = secondsElapsed / 60
        val seconds = secondsElapsed % 60
        val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)
        customView.setTextViewText(R.id.notif_timer, formattedTime)

        // Realtime dynamic waveform bitmap
        val waveformBitmap = WaveformBitmapGenerator.createWaveformBitmap(
            rmsDb = currentRms,
            isCapturingSound = isCapturingSound,
            isPaused = isPaused,
            animFrame = animFrame
        )
        customView.setImageViewBitmap(R.id.notif_waveform, waveformBitmap)

        // Intent to launch MainActivity when clicking the notification body
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause Action PendingIntent
        val playPauseAction = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        val playPauseIntent = Intent(this, LiveTranslationService::class.java).apply {
            this.action = playPauseAction
            this.putExtra("FROM_NOTIFICATION", true)
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set Play or Pause icon on the button
        if (isPaused) {
            customView.setImageViewResource(R.id.notif_btn_play_pause, R.drawable.ic_notif_play)
        } else {
            customView.setImageViewResource(R.id.notif_btn_play_pause, R.drawable.ic_notif_pause)
        }
        customView.setOnClickPendingIntent(R.id.notif_btn_play_pause, playPausePendingIntent)

        // Stop Action PendingIntent
        val stopIntent = Intent(this, LiveTranslationService::class.java).apply {
            this.action = ACTION_STOP
            this.putExtra("FROM_NOTIFICATION", true)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        customView.setOnClickPendingIntent(R.id.notif_btn_stop, stopPendingIntent)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_translate)
            .setContentIntent(openAppPendingIntent)
            .setCustomContentView(customView)
            .setCustomBigContentView(customView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing live audio translation and recording session"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopForegroundService() {
        timerJob?.cancel()
        timerJob = null
        visualizerJob?.cancel()
        visualizerJob = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
