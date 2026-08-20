package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: String? = null
    private var isRecording = false

    fun startRecording(): String? {
        try {
            stopRecording() // Clean up any existing session
            val audioDir = File(context.filesDir, "audio_recordings").apply { mkdirs() }
            val outputFile = File(audioDir, "session_${System.currentTimeMillis()}.m4a")
            currentOutputFile = outputFile.absolutePath

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            Log.d("AudioRecorder", "Recording started: ${outputFile.absolutePath}")
            return currentOutputFile
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start audio recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            return null
        }
    }

    fun pauseRecording() {
        if (isRecording && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Error pausing recorder", e)
            }
        }
    }

    fun resumeRecording() {
        if (isRecording && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Error resuming recorder", e)
            }
        }
    }

    fun stopRecording(): String? {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Error stopping recorder", e)
            } finally {
                mediaRecorder = null
                isRecording = false
            }
            val recordedPath = currentOutputFile
            Log.d("AudioRecorder", "Recording stopped: $recordedPath")
            return recordedPath
        }
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        return currentOutputFile
    }
}
