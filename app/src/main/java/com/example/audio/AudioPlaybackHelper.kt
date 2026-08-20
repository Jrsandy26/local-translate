package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlaybackHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0)
    val totalDurationMs = _totalDurationMs.asStateFlow()

    private val _activeSegmentIndex = MutableStateFlow(-1)
    val activeSegmentIndex = _activeSegmentIndex.asStateFlow()

    fun playAudioFile(
        filePath: String,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        stop()
        try {
            val file = File(filePath)
            if (!file.exists()) {
                onError?.invoke("Audio file does not exist")
                return
            }

            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = duration
                    stopProgressUpdates()
                    onComplete?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayback", "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    stopProgressUpdates()
                    onError?.invoke("Audio playback error")
                    true
                }
                start()
            }

            mediaPlayer = player
            _isPlaying.value = true
            _totalDurationMs.value = player.duration
            _currentPositionMs.value = 0
            startProgressUpdates()
        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error playing audio file", e)
            _isPlaying.value = false
            onError?.invoke(e.message ?: "Failed to play audio")
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                stopProgressUpdates()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error pausing playback", e)
        }
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressUpdates()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error resuming playback", e)
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _currentPositionMs.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error seeking playback", e)
        }
    }

    fun stop() {
        try {
            stopProgressUpdates()
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error stopping playback", e)
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _currentPositionMs.value = 0
        }
    }

    fun setActiveSegment(index: Int) {
        _activeSegmentIndex.value = index
    }

    fun setSimulatedDuration(durationSeconds: Int) {
        _totalDurationMs.value = durationSeconds * 1000
    }

    fun updateProgressManually(currentMs: Int, totalMs: Int) {
        _currentPositionMs.value = currentMs
        _totalDurationMs.value = totalMs
    }

    fun setPlayingState(playing: Boolean) {
        _isPlaying.value = playing
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition
                        mainHandler.postDelayed(this, 100)
                    }
                }
            }
        }
        mainHandler.post(progressRunnable!!)
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { mainHandler.removeCallbacks(it) }
        progressRunnable = null
    }

    fun release() {
        stop()
    }
}
