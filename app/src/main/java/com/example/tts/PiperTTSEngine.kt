package com.example.tts

import android.os.Debug
import android.util.Log
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import java.io.File
import java.nio.LongBuffer
import java.nio.FloatBuffer
import kotlin.ranges.coerceIn

class PiperTTSEngine : TTSEngine {
    private var ortEnv: OrtEnvironment? = null
    private var session: OrtSession? = null
    private val phonemizer = PiperPhonemizer()
    private var isLoaded: Boolean = false

    override fun load(modelDirectory: File): Boolean {
        try {
            if (!modelDirectory.exists()) return false
            val modelFile = File(modelDirectory, "en_US-lessac-medium.onnx")
            val configFile = File(modelDirectory, "en_US-lessac-medium.onnx.json")
            if (!modelFile.exists() || !configFile.exists()) {
                Log.e("PiperTTSEngine", "Model files do not exist in: ${modelDirectory.absolutePath}")
                return false
            }
            if (!phonemizer.load(configFile)) {
                Log.e("PiperTTSEngine", "Failed to load phoneme configuration JSON")
                return false
            }
            val env = OrtEnvironment.getEnvironment()
            ortEnv = env
            val opts = OrtSession.SessionOptions()
            opts.setIntraOpNumThreads(4)
            opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            session = env.createSession(modelFile.absolutePath, opts)
            isLoaded = true
            Log.i("PiperTTSEngine", "Piper TTS ONNX model loaded successfully.")
            return true
        } catch (e: Exception) {
            Log.e("PiperTTSEngine", "Error loading Piper TTS model", e)
            return false
        }
    }

    override fun synthesize(text: String): AudioResult {
        val env = ortEnv
        val sess = session
        if (!isLoaded || env == null || sess == null) {
            return AudioResult(
                pcmData = ShortArray(0),
                sampleRate = 22050,
                text = text,
                phonemizationTimeMs = 0L,
                onnxInferenceTimeMs = 0L,
                totalTimeMs = 0L,
                audioDurationMs = 0L,
                rtf = 0.0f,
                peakMemoryMb = 0.0f
            )
        }

        val startTime = System.currentTimeMillis()
        val phonemeStartTime = System.currentTimeMillis()
        val phonemeIds = phonemizer.textToPhonemeIds(text)
        val phonemeEndTime = System.currentTimeMillis()
        val phonemizationTimeMs = phonemeEndTime - phonemeStartTime

        val length = phonemeIds.size.toLong()
        val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(phonemeIds), longArrayOf(1, length))
        
        val inputLengths = longArrayOf(length)
        val inputLengthsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputLengths), longArrayOf(1))

        val scales = floatArrayOf(0.667f, 1.0f, 0.8f)
        val scalesTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(scales), longArrayOf(3))

        val inputs = mutableMapOf(
            "input" to inputTensor,
            "input_lengths" to inputLengthsTensor,
            "scales" to scalesTensor
        )

        if (sess.inputNames.contains("sid")) {
            val sidData = longArrayOf(0L)
            val sidTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(sidData), longArrayOf(1))
            inputs["sid"] = sidTensor
        }

        val inferenceStartTime = System.currentTimeMillis()
        val result = sess.run(inputs)
        val inferenceEndTime = System.currentTimeMillis()
        val onnxInferenceTimeMs = inferenceEndTime - inferenceStartTime

        val outputValue = result[0].value
        val floatSamples = extractFloatArray(outputValue)

        val pcmData = ShortArray(floatSamples.size)
        for (i in floatSamples.indices) {
            val sample = (floatSamples[i] * 32767.0f).toInt().coerceIn(-32768, 32767)
            pcmData[i] = sample.toShort()
        }

        inputTensor.close()
        inputLengthsTensor.close()
        scalesTensor.close()
        if (inputs.containsKey("sid")) {
            (inputs["sid"] as? OnnxTensor)?.close()
        }
        result.close()

        val endTime = System.currentTimeMillis()
        val totalTimeMs = endTime - startTime
        val sampleRate = 22050
        val audioDurationMs = (pcmData.size.toLong() * 1000L) / sampleRate
        val rtf = if (audioDurationMs > 0) totalTimeMs.toFloat() / audioDurationMs.toFloat() else 0.0f
        val peakMemoryMb = Debug.getNativeHeapAllocatedSize().toFloat() / 1048576.0f

        return AudioResult(
            pcmData = pcmData,
            sampleRate = sampleRate,
            text = text,
            phonemizationTimeMs = phonemizationTimeMs,
            onnxInferenceTimeMs = onnxInferenceTimeMs,
            totalTimeMs = totalTimeMs,
            audioDurationMs = audioDurationMs,
            rtf = rtf,
            peakMemoryMb = peakMemoryMb
        )
    }

    private fun extractFloatArray(value: Any?): FloatArray {
        if (value == null) return FloatArray(0)
        if (value is FloatArray) return value
        if (value is Array<*>) {
            if (value.isEmpty()) return FloatArray(0)
            return extractFloatArray(value[0])
        }
        if (value::class.java.isArray) {
            val length = java.lang.reflect.Array.getLength(value)
            if (length == 0) return FloatArray(0)
            val firstElement = java.lang.reflect.Array.get(value, 0)
            return extractFloatArray(firstElement)
        }
        return FloatArray(0)
    }

    override fun unload() {
        try {
            session?.close()
            ortEnv?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            session = null
            ortEnv = null
            isLoaded = false
        }
    }
}
