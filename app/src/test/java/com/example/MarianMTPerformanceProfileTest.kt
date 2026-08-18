package com.example

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.translation.MarianMTTranslationEngine
import com.example.translation.SentencePieceTokenizer
import com.example.translation.TranslationModelDownloader
import com.example.tts.PiperModelDownloader
import com.example.tts.PiperTTSEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MarianMTPerformanceProfileTest {

    @Test
    fun profileMarianMTPipeline() {
        runBlocking {
            val marianDir = File("/tmp/test_marian")
        if (!marianDir.exists() || !File(marianDir, "encoder_model_quantized.onnx").exists()) {
            marianDir.mkdirs()
            val success = TranslationModelDownloader.downloadMarianMTModel(marianDir) {}
            assertTrue("Failed to download Marian model", success)
        }

        val piperDir = File("/tmp/test_piper")
        if (!piperDir.exists() || !File(piperDir, "en_US-lessac-medium.onnx").exists()) {
            piperDir.mkdirs()
            val success = PiperModelDownloader.downloadPiperModel(piperDir) {}
            assertTrue("Failed to download Piper model", success)
        }

        println("==========================================================================")
        println("               MILESTONE 4.1 — MARIANMT PROFILING & INSPECTION            ")
        println("==========================================================================")

        val env = OrtEnvironment.getEnvironment()

        // 1. Inspect ONNX Model Files & Signatures
        val encoderFile = File(marianDir, "encoder_model_quantized.onnx")
        val decoderFile = File(marianDir, "decoder_model_quantized.onnx")
        val decoderWithPastFile = File(marianDir, "decoder_with_past_model_quantized.onnx")

        println("\n--- 1. MODEL FILE SIZES ---")
        println("Encoder file size: ${encoderFile.length() / (1024 * 1024)} MB")
        println("Decoder file size: ${decoderFile.length() / (1024 * 1024)} MB")
        println("Decoder with past file size: ${decoderWithPastFile.length() / (1024 * 1024)} MB (Exists: ${decoderWithPastFile.exists()})")

        val opts = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(4)
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
        }

        val encoderSess = env.createSession(encoderFile.absolutePath, opts)
        val decoderSess = env.createSession(decoderFile.absolutePath, opts)
        val decoderWithPastSess = if (decoderWithPastFile.exists()) env.createSession(decoderWithPastFile.absolutePath, opts) else null

        println("\n--- 2. ENCODER GRAPH SIGNATURE ---")
        println("Inputs:")
        encoderSess.inputNames.forEach { name ->
            println("  - $name: ${encoderSess.inputInfo[name]?.info}")
        }
        println("Outputs:")
        encoderSess.outputNames.forEach { name ->
            println("  - $name: ${encoderSess.outputInfo[name]?.info}")
        }

        println("\n--- 3. DECODER GRAPH SIGNATURE ---")
        println("Inputs:")
        decoderSess.inputNames.forEach { name ->
            println("  - $name: ${decoderSess.inputInfo[name]?.info}")
        }
        println("Outputs:")
        decoderSess.outputNames.forEach { name ->
            println("  - $name: ${decoderSess.outputInfo[name]?.info}")
        }

        if (decoderWithPastSess != null) {
            println("\n--- 4. DECODER WITH PAST GRAPH SIGNATURE ---")
            println("Inputs:")
            decoderWithPastSess.inputNames.forEach { name ->
                println("  - $name: ${decoderWithPastSess.inputInfo[name]?.info}")
            }
            println("Outputs:")
            decoderWithPastSess.outputNames.forEach { name ->
                println("  - $name: ${decoderWithPastSess.outputInfo[name]?.info}")
            }
        }

        // Tokenizer profile
        val tokenizer = SentencePieceTokenizer()
        val vocabFile = File(marianDir, "vocab.json")
        val tokLoadStart = System.currentTimeMillis()
        tokenizer.load(vocabFile)
        val tokLoadEnd = System.currentTimeMillis()
        println("\n--- 5. TOKENIZER PROFILE ---")
        println("Vocab load time: ${tokLoadEnd - tokLoadStart} ms")

        val testText = "今日は工場の生産ラインを確認して、問題があれば改善します。"
        val tokStart = System.nanoTime()
        val inputTokens = tokenizer.tokenize(testText)
        val tokEnd = System.nanoTime()
        val tokTimeMs = (tokEnd - tokStart) / 1_000_000.0

        println("Sample Text: \"$testText\"")
        println("Characters: ${testText.length}")
        println("Input Tokens: ${inputTokens.size} (${inputTokens.joinToString(",")})")
        println("Tokenization Time: %.3f ms".format(tokTimeMs))

        encoderSess.close()
        decoderSess.close()
        decoderWithPastSess?.close()
        }
    }
}
