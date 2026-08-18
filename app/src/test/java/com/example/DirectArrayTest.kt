package com.example

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.translation.SentencePieceTokenizer
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DirectArrayTest {

    @Test
    fun testDirectArrayAccess() {
        runBlocking {
            val marianDir = File("/tmp/test_marian")
            val env = OrtEnvironment.getEnvironment()
            val vocabFile = File(marianDir, "vocab.json")
            val tokenizer = SentencePieceTokenizer()
            tokenizer.load(vocabFile)

            val opts = OrtSession.SessionOptions().apply { setIntraOpNumThreads(2) }
            val encoderFile = File(marianDir, "encoder_model_quantized.onnx")
            val decoderFile = File(marianDir, "decoder_model_quantized.onnx")

            val encoderSess = env.createSession(encoderFile.absolutePath, opts)
            val decoderSess = env.createSession(decoderFile.absolutePath, opts)

            // --- 'こんにちは。' DIAGNOSTICS ---
            val helloText = "こんにちは。"
            val helloTokens = tokenizer.tokenize(helloText)
            val helloMask = LongArray(helloTokens.size) { 1L }
            val helloInputIdsTensor = OnnxTensor.createTensor(env, arrayOf(helloTokens))
            val helloAttentionMaskTensor = OnnxTensor.createTensor(env, arrayOf(helloMask))
            val helloEncoderResult = encoderSess.run(mapOf("input_ids" to helloInputIdsTensor, "attention_mask" to helloAttentionMaskTensor))
            val helloEncoderHiddenState = helloEncoderResult.get("last_hidden_state").get() as OnnxTensor
            val helloDecInputIds0 = OnnxTensor.createTensor(env, arrayOf(longArrayOf(60715L)))
            val helloDecResult0 = decoderSess.run(
                mapOf("input_ids" to helloDecInputIds0, "encoder_hidden_states" to helloEncoderHiddenState, "encoder_attention_mask" to helloAttentionMaskTensor)
            )
            val helloLogitsTensor0 = helloDecResult0.get("logits").get() as OnnxTensor
            val helloArray3D = helloLogitsTensor0.value as Array<Array<FloatArray>>
            val helloFirstLogits = helloArray3D[0][0]
            var helloNanCount = 0
            var helloZeroCount = 0
            var helloPositiveCount = 0
            var helloNegativeCount = 0
            for (v in helloFirstLogits) {
                if (v.isNaN()) helloNanCount++
                else if (v == 0.0f) helloZeroCount++
                else if (v > 0.0f) helloPositiveCount++
                else if (v < 0.0f) helloNegativeCount++
            }
            println("=== HELLO LOGITS DIAGNOSTICS ===")
            println("Total hello logits: ${helloFirstLogits.size}")
            println("NaNs: $helloNanCount")
            println("Zeros: $helloZeroCount")
            println("Positives: $helloPositiveCount")
            println("Negatives: $helloNegativeCount")
            var helloMaxVal = -Float.MAX_VALUE
            var helloMaxIdx = 0L
            for (i in helloFirstLogits.indices) {
                if (helloFirstLogits[i] > helloMaxVal) {
                    helloMaxVal = helloFirstLogits[i]
                    helloMaxIdx = i.toLong()
                }
            }
            println("Hello Step 0: maxIdx=$helloMaxIdx maxVal=$helloMaxVal")
            // Close hello tensors
            helloInputIdsTensor.close()
            helloAttentionMaskTensor.close()
            helloEncoderResult.close()
            helloDecInputIds0.close()
            helloDecResult0.close()

            val jaText = "今日は工場の生産ラインを確認して、問題があれば改善します。"
            val inputTokens = tokenizer.tokenize(jaText)
            
            println("=== ORIGINAL SENTENCE SUBWORDS ===")
            for (id in inputTokens) {
                val subword = tokenizer.detokenize(listOf(id))
                println("ID: $id -> Subword: '$subword'")
            }

            val inputBuf = java.nio.ByteBuffer.allocateDirect(inputTokens.size * 8).order(java.nio.ByteOrder.nativeOrder()).asLongBuffer()
            inputBuf.put(inputTokens).rewind()

            val maskBuf = java.nio.ByteBuffer.allocateDirect(inputTokens.size * 8).order(java.nio.ByteOrder.nativeOrder()).asLongBuffer()
            val maskArray = LongArray(inputTokens.size) { 1L }
            maskBuf.put(maskArray).rewind()

            val inputIdsTensor = OnnxTensor.createTensor(env, arrayOf(inputTokens))
            val attentionMaskTensor = OnnxTensor.createTensor(env, arrayOf(maskArray))

            val encoderResult = encoderSess.run(mapOf("input_ids" to inputIdsTensor, "attention_mask" to attentionMaskTensor))
            val encoderHiddenState = encoderResult.get("last_hidden_state").get() as OnnxTensor

            println("Encoder Hidden State Shape: ${encoderHiddenState.info.shape.contentToString()}")
            val encVal = encoderHiddenState.value
            println("Encoder Value Class: ${encVal.javaClass.name}")
            if (encVal is Array<*>) {
                val array3D = encVal as Array<Array<FloatArray>>
                println("Encoder Hidden State [0][0][:5]: ${array3D[0][0].take(5)}")
                
                var encNanCount = 0
                for (seq in array3D[0]) {
                    for (v in seq) {
                        if (v.isNaN()) encNanCount++
                    }
                }
                println("Encoder NaNs count: $encNanCount")
            }

            val decInputIds0 = OnnxTensor.createTensor(env, java.nio.LongBuffer.wrap(longArrayOf(60715L)), longArrayOf(1, 1))

            val decResult0 = decoderSess.run(
                mapOf("input_ids" to decInputIds0, "encoder_hidden_states" to encoderHiddenState, "encoder_attention_mask" to attentionMaskTensor)
            )

            val logitsTensor0 = decResult0.get("logits").get() as OnnxTensor
            val value = logitsTensor0.value

            println("Value Class: ${value.javaClass.name}")

            val decoderWithPastFile = File(marianDir, "decoder_with_past_model_quantized.onnx")
            val decoderWithPastSess = env.createSession(decoderWithPastFile.absolutePath, opts)

            val array3D = logitsTensor0.value as Array<Array<FloatArray>>
            val firstLogits = array3D[0][0]
            
            var nanCount = 0
            var zeroCount = 0
            var positiveCount = 0
            var negativeCount = 0
            for (v in firstLogits) {
                if (v.isNaN()) nanCount++
                else if (v == 0.0f) zeroCount++
                else if (v > 0.0f) positiveCount++
                else if (v < 0.0f) negativeCount++
            }
            println("=== LOGITS DIAGNOSTICS ===")
            println("Total logits: ${firstLogits.size}")
            println("NaNs: $nanCount")
            println("Zeros: $zeroCount")
            println("Positives: $positiveCount")
            println("Negatives: $negativeCount")
            if (firstLogits.size > 10) {
                println("First 10 logits: ${firstLogits.take(10)}")
            }

            var maxVal = -Float.MAX_VALUE
            var maxIdx = 0L
            for (i in firstLogits.indices) {
                if (i == 60715) continue
                if (firstLogits[i] > maxVal) {
                    maxVal = firstLogits[i]
                    maxIdx = i.toLong()
                }
            }

            println("Direct Array Step 0: maxIdx=$maxIdx maxVal=$maxVal")

            var currentPastKeyValues = mutableMapOf<String, OnnxTensor>()
            for (i in 0 until 6) {
                currentPastKeyValues["past_key_values.$i.decoder.key"] = decResult0.get("present.$i.decoder.key").get() as OnnxTensor
                currentPastKeyValues["past_key_values.$i.decoder.value"] = decResult0.get("present.$i.decoder.value").get() as OnnxTensor
                currentPastKeyValues["past_key_values.$i.encoder.key"] = decResult0.get("present.$i.encoder.key").get() as OnnxTensor
                currentPastKeyValues["past_key_values.$i.encoder.value"] = decResult0.get("present.$i.encoder.value").get() as OnnxTensor
            }

            decInputIds0.close()

            val generatedTokens = mutableListOf<Long>()
            if (maxIdx != 0L) generatedTokens.add(maxIdx)
            var lastTokenId = maxIdx
            val tensorsToClose = mutableListOf<OnnxTensor>()

            for (step in 1 until 30) {
                if (lastTokenId == 0L) break

                val curInputIdsTensor = OnnxTensor.createTensor(env, java.nio.LongBuffer.wrap(longArrayOf(lastTokenId)), longArrayOf(1, 1))
                val decWithPastInputs = mutableMapOf<String, OnnxTensor>(
                    "input_ids" to curInputIdsTensor,
                    "encoder_attention_mask" to attentionMaskTensor
                )
                decWithPastInputs.putAll(currentPastKeyValues)

                val decWithPastResult = decoderWithPastSess.run(decWithPastInputs)
                val curLogitsTensor = decWithPastResult.get("logits").get() as OnnxTensor
                val curArray3D = curLogitsTensor.value as Array<Array<FloatArray>>
                val stepLogits = curArray3D[0][0]

                var maxL = -Float.MAX_VALUE
                var maxT = 0L
                for (i in stepLogits.indices) {
                    if (i == 60715) continue
                    if (stepLogits[i] > maxL) {
                        maxL = stepLogits[i]
                        maxT = i.toLong()
                    }
                }

                println("Step $step: tokenId=$maxT logit=$maxL")

                val nextPast = mutableMapOf<String, OnnxTensor>()
                for (i in 0 until 6) {
                    val decKey = decWithPastResult.get("present.$i.decoder.key").get() as OnnxTensor
                    val decVal = decWithPastResult.get("present.$i.decoder.value").get() as OnnxTensor
                    nextPast["past_key_values.$i.decoder.key"] = decKey
                    nextPast["past_key_values.$i.decoder.value"] = decVal
                    nextPast["past_key_values.$i.encoder.key"] = currentPastKeyValues["past_key_values.$i.encoder.key"]!!
                    nextPast["past_key_values.$i.encoder.value"] = currentPastKeyValues["past_key_values.$i.encoder.value"]!!
                    tensorsToClose.add(decKey)
                    tensorsToClose.add(decVal)
                }

                curInputIdsTensor.close()
                currentPastKeyValues = nextPast
                lastTokenId = maxT
                if (lastTokenId != 0L) generatedTokens.add(lastTokenId)
            }

            val english = tokenizer.detokenize(generatedTokens)
            println("TRANSLATION RESULT: \"$english\"")
        }
    }
}
