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
import java.nio.FloatBuffer
import java.nio.LongBuffer

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MarianMTOptimizationBenchmark {

    @Test
    fun benchmarkKVCacheAndThreads() {
        runBlocking {
            val marianDir = File("/tmp/test_marian")
            if (!marianDir.exists() || !File(marianDir, "encoder_model_quantized.onnx").exists()) {
                marianDir.mkdirs()
                val success = TranslationModelDownloader.downloadMarianMTModel(marianDir) {}
                assertTrue("Failed to download Marian model", success)
            }

            val env = OrtEnvironment.getEnvironment()
            val vocabFile = File(marianDir, "vocab.json")
            val tokenizer = SentencePieceTokenizer()
            tokenizer.load(vocabFile)

            val encoderFile = File(marianDir, "encoder_model_quantized.onnx")
            val decoderFile = File(marianDir, "decoder_model_quantized.onnx")
            val decoderWithPastFile = File(marianDir, "decoder_with_past_model_quantized.onnx")

            println("==========================================================================")
            println("            MILESTONE 4.1 BENCHMARK: KV CACHE & THREAD OPTIMIZATION        ")
            println("==========================================================================")

            val threadConfigs = listOf(1, 2, 4)

            for (threads in threadConfigs) {
                val opts = OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(threads)
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
                }

                val encoderSess = env.createSession(encoderFile.absolutePath, opts)
                val decoderSess = env.createSession(decoderFile.absolutePath, opts)
                val decoderWithPastSess = env.createSession(decoderWithPastFile.absolutePath, opts)

                val testSentences = listOf(
                    "Short sentence" to "こんにちは。",
                    "Business Japanese" to "明日の会議の資料をメールで送付いたしました。",
                    "Manufacturing Japanese" to "今日は工場の生産ラインを確認して、問題があれば改善します。",
                    "Technical Japanese" to "サーバーのデータベースをバックアップして同期処理を実行します。",
                    "Numbers" to "第3ラインの稼働率は85パーセントで、不具合は2件です。",
                    "Long sentence" to "当社は新しい自動化システムを導入することで、生産効率を大幅に向上させ、現場の作業負担を軽減することを目指しています。"
                )

                println("\n==========================================================")
                println("   BENCHMARKING WITH INTRA_OP_THREADS = $threads")
                println("==========================================================")

                for ((category, jaText) in testSentences) {
                    val tokStart = System.currentTimeMillis()
                    val inputTokens = tokenizer.tokenize(jaText)
                    val tokEnd = System.currentTimeMillis()
                    val tokTime = tokEnd - tokStart

                    val seqLen = inputTokens.size.toLong()
                    val inputShape = longArrayOf(1, seqLen)
                    val attentionMaskArray = LongArray(inputTokens.size) { 1L }

                    val inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputTokens), inputShape)
                    val attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMaskArray), inputShape)

                    // 1. Encoder
                    val encStart = System.currentTimeMillis()
                    val encoderResult = encoderSess.run(
                        mapOf(
                            "input_ids" to inputIdsTensor,
                            "attention_mask" to attentionMaskTensor
                        )
                    )
                    val encEnd = System.currentTimeMillis()
                    val encTime = encEnd - encStart

                    val encoderHiddenState = encoderResult.get("last_hidden_state").get() as OnnxTensor

                    // 2. Decoder with KV Cache
                    val decStart = System.currentTimeMillis()

                    val startTokenId = 60715L // <pad> / start
                    val decInputIds0 = OnnxTensor.createTensor(env, LongBuffer.wrap(longArrayOf(startTokenId)), longArrayOf(1, 1))

                    val decInputs0 = mapOf(
                        "input_ids" to decInputIds0,
                        "encoder_hidden_states" to encoderHiddenState,
                        "encoder_attention_mask" to attentionMaskTensor
                    )

                    val decResult0 = decoderSess.run(decInputs0)

                    val logitsTensor0 = decResult0.get("logits").get() as OnnxTensor
                    val vocabSize = 60716

                    // Direct FloatBuffer extraction avoiding array reflection copies
                    val fb0 = logitsTensor0.floatBuffer
                    var maxLogit0 = -Float.MAX_VALUE
                    var maxTokenId0 = 0L
                    for (v in 0 until vocabSize) {
                        if (v == 60715) continue // Suppress <pad> token during generation
                        val logit = fb0.get(v)
                        if (logit > maxLogit0) {
                            maxLogit0 = logit
                            maxTokenId0 = v.toLong()
                        }
                    }

                    val generatedTokens = mutableListOf<Long>()
                    if (maxTokenId0 != 0L) {
                        generatedTokens.add(maxTokenId0)
                    }

                    // Extract and cache key-values from Step 0
                    var currentPastKeyValues = mutableMapOf<String, OnnxTensor>()
                    for (i in 0 until 6) {
                        currentPastKeyValues["past_key_values.$i.decoder.key"] = decResult0.get("present.$i.decoder.key").get() as OnnxTensor
                        currentPastKeyValues["past_key_values.$i.decoder.value"] = decResult0.get("present.$i.decoder.value").get() as OnnxTensor
                        currentPastKeyValues["past_key_values.$i.encoder.key"] = decResult0.get("present.$i.encoder.key").get() as OnnxTensor
                        currentPastKeyValues["past_key_values.$i.encoder.value"] = decResult0.get("present.$i.encoder.value").get() as OnnxTensor
                    }

                    decInputIds0.close()

                    val maxNewTokens = 128
                    var lastTokenId = maxTokenId0
                    var tokenCount = 1

                    val tensorsToClose = mutableListOf<OnnxTensor>()

                    for (step in 1 until maxNewTokens) {
                        if (lastTokenId == 0L) break

                        val curInputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(longArrayOf(lastTokenId)), longArrayOf(1, 1))

                        val decWithPastInputs = mutableMapOf<String, OnnxTensor>(
                            "input_ids" to curInputIdsTensor,
                            "encoder_attention_mask" to attentionMaskTensor
                        )
                        decWithPastInputs.putAll(currentPastKeyValues)

                        val decWithPastResult = decoderWithPastSess.run(decWithPastInputs)
                        tokenCount++

                        val curLogitsTensor = decWithPastResult.get("logits").get() as OnnxTensor
                        val cfb = curLogitsTensor.floatBuffer

                        var maxL = -Float.MAX_VALUE
                        var maxT = 0L
                        for (v in 0 until vocabSize) {
                            if (v == 60715) continue // Suppress <pad> token during generation
                            val logit = cfb.get(v)
                            if (logit > maxL) {
                                maxL = logit
                                maxT = v.toLong()
                            }
                        }

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

                        if (lastTokenId != 0L) {
                            generatedTokens.add(lastTokenId)
                        }
                    }

                    val decEnd = System.currentTimeMillis()
                    val decTime = decEnd - decStart

                    val detokStart = System.currentTimeMillis()
                    val english = tokenizer.detokenize(generatedTokens)
                    val detokEnd = System.currentTimeMillis()
                    val detokTime = detokEnd - detokStart

                    val totalTime = tokTime + encTime + decTime + detokTime

                    println("[$category]")
                    println("  Input: \"$jaText\" (${inputTokens.size} tokens)")
                    println("  Output: \"$english\" (${generatedTokens.size} tokens)")
                    println("  Timing: Tok=${tokTime}ms | Enc=${encTime}ms | Dec=${decTime}ms (${String.format("%.1f", decTime.toFloat()/tokenCount)}ms/tok) | Detok=${detokTime}ms | Total=${totalTime}ms")

                    inputIdsTensor.close()
                    attentionMaskTensor.close()
                    encoderResult.close()
                    decResult0.close()
                    tensorsToClose.forEach { try { it.close() } catch (e: Exception) {} }
                }

                encoderSess.close()
                decoderSess.close()
                decoderWithPastSess.close()
            }
        }
    }
}
