package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.math.log10
import kotlin.math.sqrt

class AudioRecorderHelper(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var mediaPlayer: MediaPlayer? = null
    
    @Volatile
    var isRecording = false
        private set

    var currentAudioFilePath: String? = null
        private set

    private var recordingThread: Thread? = null
    private var tempPcmFilePath: String? = null

    // WAV Format Configuration
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording(sessionId: Long, onRmsChanged: (Float) -> Unit = {}): String? {
        if (!hasAudioPermission()) {
            Log.e("AudioRecorderHelper", "Missing RECORD_AUDIO permission")
            return null
        }
        if (isRecording) {
            stopRecording()
        }

        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val finalWavFile = File(recordingsDir, "session_${sessionId}_${System.currentTimeMillis()}.wav")
        val tempPcmFile = File(recordingsDir, "session_${sessionId}_temp.pcm")

        currentAudioFilePath = finalWavFile.absolutePath
        tempPcmFilePath = tempPcmFile.absolutePath

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE.coerceAtLeast(2048)
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioRecorderHelper", "AudioRecord initialization failed")
                return null
            }

            audioRecord?.startRecording()
            isRecording = true

            recordingThread = thread(start = true, name = "AudioRecord-Thread") {
                writeAudioDataToTempFile(tempPcmFile, onRmsChanged)
                convertPcmToWav(tempPcmFile, finalWavFile)
            }

            return currentAudioFilePath
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error starting AudioRecord", e)
            isRecording = false
            currentAudioFilePath = null
            tempPcmFilePath = null
            return null
        }
    }

    private fun writeAudioDataToTempFile(tempFile: File, onRmsChanged: (Float) -> Unit) {
        val buffer = ShortArray(BUFFER_SIZE / 2)
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(tempFile)
            while (isRecording) {
                val readStatus = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (readStatus > 0) {
                    // Compute live RMS value from buffer shorts
                    var sum = 0.0
                    for (i in 0 until readStatus) {
                        sum += buffer[i] * buffer[i]
                    }
                    val mean = sum / readStatus
                    val rms = sqrt(mean)

                    // Map RMS to standard logarithmic scale 0f to 10f for visualizer
                    val rmsDb = if (rms > 0) 20 * log10(rms / 32768.0) + 90 else 0.0
                    val mappedRms = (rmsDb.toFloat() / 9f).coerceIn(0f, 10f)

                    onRmsChanged(mappedRms)

                    // Convert short array to byte array for raw PCM file write
                    val byteBuffer = ByteArray(readStatus * 2)
                    for (i in 0 until readStatus) {
                        byteBuffer[i * 2] = (buffer[i].toInt() and 0xff).toByte()
                        byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xff).toByte()
                    }
                    os.write(byteBuffer)
                }
            }
        } catch (e: IOException) {
            Log.e("AudioRecorderHelper", "Error writing raw PCM data to temp file", e)
        } finally {
            try {
                os?.close()
            } catch (_: Exception) {}
        }
    }

    private fun convertPcmToWav(pcmFile: File, wavFile: File) {
        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        val totalAudioLen: Long
        val totalDataLen: Long
        val channels = 1
        val byteRate = SAMPLE_RATE * channels * 2

        val buffer = ByteArray(BUFFER_SIZE)

        try {
            fis = FileInputStream(pcmFile)
            fos = FileOutputStream(wavFile)
            totalAudioLen = pcmFile.length()
            totalDataLen = totalAudioLen + 36

            // Write wave header
            writeWavHeader(fos, totalAudioLen, totalDataLen, SAMPLE_RATE.toLong(), channels, byteRate.toLong())

            var read = fis.read(buffer)
            while (read != -1) {
                fos.write(buffer, 0, read)
                read = fis.read(buffer)
            }
        } catch (e: IOException) {
            Log.e("AudioRecorderHelper", "Error converting PCM to WAV", e)
        } finally {
            try {
                fis?.close()
                fos?.close()
            } catch (_: Exception) {}
            // Clean up raw temp file
            if (pcmFile.exists()) {
                pcmFile.delete()
            }
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        longSampleRate: Long,
        channels: Int,
        byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte() // WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // fmt 
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // format subchunk size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // Audio format 1 = PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte() // Block align (channels * bytes per sample)
        header[33] = 0
        header[34] = 16 // 16 bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() // data chunk
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }

    fun stopRecording(): String? {
        if (!isRecording) return currentAudioFilePath
        isRecording = false

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping AudioRecord", e)
        } finally {
            audioRecord = null
        }

        try {
            recordingThread?.join(1000)
        } catch (_: InterruptedException) {}
        recordingThread = null

        return currentAudioFilePath
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
