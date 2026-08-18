package com.example

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.translation.SentencePieceTokenizer
import com.example.translation.TranslationModelDownloader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.LongBuffer

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ManufacturingDiagnosticTest {

    @Test
    fun traceManufacturingTranslation() {
        runBlocking {
            val marianDir = File("/tmp/test_marian")
            if (!marianDir.exists() || !File(marianDir, "encoder_model_quantized.onnx").exists() || !File(marianDir, "vocab.json").exists()) {
                marianDir.mkdirs()
                val success = TranslationModelDownloader.downloadMarianMTModel(marianDir) {}
                assertTrue("Failed to download Marian model", success)
            }
            val env = OrtEnvironment.getEnvironment()
            val vocabFile = File(marianDir, "vocab.json")
            val tokenizer = SentencePieceTokenizer()
            tokenizer.load(vocabFile)

            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
            }

            val encoderFile = File(marianDir, "encoder_model_quantized.onnx")
            val decoderFile = File(marianDir, "decoder_model_quantized.onnx")
            val decoderWithPastFile = File(marianDir, "decoder_with_past_model_quantized.onnx")

            val encoderSess = env.createSession(encoderFile.absolutePath, opts)
            val decoderSess = env.createSession(decoderFile.absolutePath, opts)
            val decoderWithPastSess = env.createSession(decoderWithPastFile.absolutePath, opts)

            val jaText = "今日は工場の生産ラインを確認して、問題があれば改善します。"
            val inputTokens = tokenizer.tokenize(jaText)

            val seqLen = inputTokens.size.toLong()
            val inputShape = longArrayOf(1, seqLen)
            val attentionMaskArray = LongArray(inputTokens.size) { 1L }

            val inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputTokens), inputShape)
            val attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMaskArray), inputShape)

            val encoderResult = encoderSess.run(
                mapOf("input_ids" to inputIdsTensor, "attention_mask" to attentionMaskTensor)
            )
            val encoderHiddenState = encoderResult.get("last_hidden_state").get() as OnnxTensor

            val startTokenId = 60715L
            val decInputIds0 = OnnxTensor.createTensor(env, LongBuffer.wrap(longArrayOf(startTokenId)), longArrayOf(1, 1))

            val decResult0 = decoderSess.run(
                mapOf(
                    "input_ids" to decInputIds0,
                    "encoder_hidden_states" to encoderHiddenState,
                    "encoder_attention_mask" to attentionMaskTensor
                )
            )

            val logitsTensor0 = decResult0.get("logits").get() as OnnxTensor
            @Suppress("UNCHECKED_CAST")
            val array3D0 = logitsTensor0.value as Array<Array<FloatArray>>
            val logits0 = array3D0[0][0]

            var maxLogit0 = -Float.MAX_VALUE
            var maxTokenId0 = 0L
            for (v in 0 until 60716) {
                val logit = logits0[v]
                if (logit > maxLogit0) {
                    maxLogit0 = logit
                    maxTokenId0 = v.toLong()
                }
            }

            println("Step 0 maxTokenId: $maxTokenId0 maxLogit: $maxLogit0")

            // Copy helper
            fun copyOnnxTensor(env: OrtEnvironment, tensor: OnnxTensor): OnnxTensor {
                val shape = tensor.info.shape
                val fb = tensor.floatBuffer
                val copyBuffer = java.nio.FloatBuffer.allocate(fb.remaining())
                copyBuffer.put(fb)
                copyBuffer.rewind()
                return OnnxTensor.createTensor(env, copyBuffer, shape)
            }

            var encoderKeyValues = mutableMapOf<String, OnnxTensor>()
            var decoderKeyValues = mutableMapOf<String, OnnxTensor>()

            for (i in 0 until 6) {
                val encKey = decResult0.get("present.$i.encoder.key").get() as OnnxTensor
                val encVal = decResult0.get("present.$i.encoder.value").get() as OnnxTensor
                val decKey = decResult0.get("present.$i.decoder.key").get() as OnnxTensor
                val decVal = decResult0.get("present.$i.decoder.value").get() as OnnxTensor

                encoderKeyValues["past_key_values.$i.encoder.key"] = copyOnnxTensor(env, encKey)
                encoderKeyValues["past_key_values.$i.encoder.value"] = copyOnnxTensor(env, encVal)
                decoderKeyValues["past_key_values.$i.decoder.key"] = copyOnnxTensor(env, decKey)
                decoderKeyValues["past_key_values.$i.decoder.value"] = copyOnnxTensor(env, decVal)
            }

            decInputIds0.close()
            decResult0.close()

            val generatedTokens = mutableListOf<Long>(maxTokenId0)
            var lastTokenId = maxTokenId0

            for (step in 1 until 30) {
                if (lastTokenId == 0L) break

                val curInputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(longArrayOf(lastTokenId)), longArrayOf(1, 1))
                val decWithPastInputs = mutableMapOf<String, OnnxTensor>(
                    "input_ids" to curInputIdsTensor,
                    "encoder_attention_mask" to attentionMaskTensor
                )
                decWithPastInputs.putAll(encoderKeyValues)
                decWithPastInputs.putAll(decoderKeyValues)

                val decWithPastResult = decoderWithPastSess.run(decWithPastInputs)
                val curLogitsTensor = decWithPastResult.get("logits").get() as OnnxTensor
                @Suppress("UNCHECKED_CAST")
                val curArray3D = curLogitsTensor.value as Array<Array<FloatArray>>
                val curFloats = curArray3D[0][0]

                var maxL = -Float.MAX_VALUE
                var maxT = 0L
                for (v in 0 until 60716) {
                    val logit = curFloats[v]
                    if (logit > maxL) {
                        maxL = logit
                        maxT = v.toLong()
                    }
                }

                println("Step $step: tokenId=$maxT logit=$maxL")

                val nextDecoderKeyValues = mutableMapOf<String, OnnxTensor>()
                for (i in 0 until 6) {
                    val decKey = decWithPastResult.get("present.$i.decoder.key").get() as OnnxTensor
                    val decVal = decWithPastResult.get("present.$i.decoder.value").get() as OnnxTensor
                    nextDecoderKeyValues["past_key_values.$i.decoder.key"] = copyOnnxTensor(env, decKey)
                    nextDecoderKeyValues["past_key_values.$i.decoder.value"] = copyOnnxTensor(env, decVal)
                }

                curInputIdsTensor.close()
                decWithPastResult.close()

                // Close old decoder key values
                decoderKeyValues.values.forEach { it.close() }
                decoderKeyValues = nextDecoderKeyValues

                lastTokenId = maxT
                if (lastTokenId != 0L) generatedTokens.add(lastTokenId)
            }

            val english = tokenizer.detokenize(generatedTokens)
            println("Final Diagnostic English: \"$english\"")

            inputIdsTensor.close()
            attentionMaskTensor.close()
            encoderResult.close()
            encoderKeyValues.values.forEach { it.close() }
            decoderKeyValues.values.forEach { it.close() }
            encoderSess.close()
            decoderSess.close()
            decoderWithPastSess.close()
        }
    }
}
