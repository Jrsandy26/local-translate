package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var isRecording = false
        private set
    var currentAudioFilePath: String? = null
        private set

    fun startRecording(sessionId: Long): String? {
        if (isRecording) stopRecording()

        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val outputFile = File(recordingsDir, "session_${sessionId}_${System.currentTimeMillis()}.m4a")
        currentAudioFilePath = outputFile.absolutePath

        return try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            currentAudioFilePath
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start MediaRecorder", e)
            isRecording = false
            currentAudioFilePath = null
            null
        }
    }

    fun stopRecording(): String? {
        val path = currentAudioFilePath
        if (!isRecording && mediaRecorder == null) return path

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
        return path
    }

    fun playAudio(filePath: String, onCompletion: () -> Unit = {}) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) {
            onCompletion()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener { onCompletion() }
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error playing audio file", e)
            onCompletion()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}
