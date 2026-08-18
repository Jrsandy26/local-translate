package com.example.translation

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.os.Debug
import android.util.Log
import java.io.File
import java.nio.LongBuffer

class MarianMTTranslationEngine : TranslationEngine {

    private var ortEnv: OrtEnvironment? = null
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null
    private var decoderWithPastSession: OrtSession? = null

    private val tokenizer = SentencePieceTokenizer()
    private var isLoaded = false

    override fun load(modelDirectory: File): Boolean {
        return try {
            if (!modelDirectory.exists()) return false

            val encoderFile = File(modelDirectory, "encoder_model_quantized.onnx")
            val decoderFile = File(modelDirectory, "decoder_model_quantized.onnx")
            val decoderWithPastFile = File(modelDirectory, "decoder_with_past_model_quantized.onnx")
            val vocabFile = File(modelDirectory, "vocab.json")

            if (!encoderFile.exists() || !decoderFile.exists() || !vocabFile.exists()) {
                Log.e("MarianMTEngine", "Required model files missing in ${modelDirectory.absolutePath}")
                return false
            }

            // Load tokenizer vocab
            val tokenLoaded = tokenizer.load(vocabFile)
            if (!tokenLoaded) {
                Log.e("MarianMTEngine", "Failed to load vocab.json")
                return false
            }

            val env = OrtEnvironment.getEnvironment()
            ortEnv = env

            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(4)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            }

            encoderSession = env.createSession(encoderFile.absolutePath, opts)
            decoderSession = env.createSession(decoderFile.absolutePath, opts)

            if (decoderWithPastFile.exists()) {
                decoderWithPastSession = env.createSession(decoderWithPastFile.absolutePath, opts)
            }

            isLoaded = true
            Log.i("MarianMTEngine", "MarianMT ONNX model sessions loaded successfully.")
            true
        } catch (e: Exception) {
            Log.e("MarianMTEngine", "Error loading MarianMT ONNX model", e)
            false
        }
    }

    private fun copyOnnxTensor(env: OrtEnvironment, tensor: OnnxTensor): OnnxTensor {
        val shape = tensor.info.shape
        val fb = tensor.floatBuffer
        val copyBuffer = java.nio.FloatBuffer.allocate(fb.remaining())
        copyBuffer.put(fb)
        copyBuffer.rewind()
        return OnnxTensor.createTensor(env, copyBuffer, shape)
    }

    override fun translate(text: String, sourceLanguage: String, targetLanguage: String): TranslationResult {
        if (!isLoaded || ortEnv == null || encoderSession == null || decoderSession == null) {
            return TranslationResult(
                translatedText = "[Error: Translation Engine Not Loaded]",
                inputTokenCount = 0,
                outputTokenCount = 0,
                encoderTimeMs = 0,
                decoderTimeMs = 0,
                totalTimeMs = 0,
                peakMemoryKb = 0
            )
        }

        val totalStartTime = System.currentTimeMillis()

        // 1. Tokenize Japanese input text
        val inputTokens = tokenizer.tokenize(text)
        val seqLen = inputTokens.size.toLong()

        val env = ortEnv!!
        val encoderSess = encoderSession!!
        val decoderSess = decoderSession!!

        // Prepare Tensors for Encoder
        val attentionMaskArray = LongArray(inputTokens.size) { 1L }

        val inputIdsTensor = OnnxTensor.createTensor(env, arrayOf(inputTokens))
        val attentionMaskTensor = OnnxTensor.createTensor(env, arrayOf(attentionMaskArray))

        // 2. Encoder Forward Pass
        val encoderStartTime = System.currentTimeMillis()
        val encoderResult = encoderSess.run(
            mapOf(
                "input_ids" to inputIdsTensor,
                "attention_mask" to attentionMaskTensor
            )
        )
        val encoderEndTime = System.currentTimeMillis()
        val encoderTimeMs = encoderEndTime - encoderStartTime

        val encoderHiddenStateValue = encoderResult.get("last_hidden_state").get() as OnnxTensor

        // 3. Autoregressive Decoder Step
        val decoderStartTime = System.currentTimeMillis()
        val generatedTokenIds = mutableListOf<Long>()
        val maxNewTokens = 128
        val vocabSize = 60716

        val decWithPastSess = decoderWithPastSession

        if (decWithPastSess != null) {
            // High Performance KV Cache Decoder Path
            val startTokenId = 60715L // <pad> start token
            val decInputIds0 = OnnxTensor.createTensor(env, arrayOf(longArrayOf(startTokenId)))

            val decInputs0 = mapOf(
                "input_ids" to decInputIds0,
                "encoder_hidden_states" to encoderHiddenStateValue,
                "encoder_attention_mask" to attentionMaskTensor
            )

            val decResult0 = decoderSess.run(decInputs0)

            val logitsTensor0 = decResult0.get("logits").get() as OnnxTensor

            @Suppress("UNCHECKED_CAST")
            val array3D0 = logitsTensor0.value as Array<Array<FloatArray>>
            val logits0 = array3D0[0][0]

            var maxLogit0 = -Float.MAX_VALUE
            var maxTokenId0 = 0L

            for (v in 0 until vocabSize) {
                if (v == 60715) continue // Suppress <pad> token during generation
                val logit = logits0[v]
                if (logit > maxLogit0) {
                    maxLogit0 = logit
                    maxTokenId0 = v.toLong()
                }
            }

            System.err.println("[MarianMTEngine] Step 0: maxTokenId0=$maxTokenId0 maxLogit0=$maxLogit0")

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

            if (maxTokenId0 != 0L) {
                generatedTokenIds.add(maxTokenId0)

                var lastTokenId = maxTokenId0

                for (step in 1 until maxNewTokens) {
                    if (lastTokenId == 0L) break

                    val curInputIdsTensor = OnnxTensor.createTensor(env, arrayOf(longArrayOf(lastTokenId)))

                    val decWithPastInputs = mutableMapOf<String, OnnxTensor>(
                        "input_ids" to curInputIdsTensor,
                        "encoder_attention_mask" to attentionMaskTensor
                    )
                    decWithPastInputs.putAll(encoderKeyValues)
                    decWithPastInputs.putAll(decoderKeyValues)

                    val stepResult = decWithPastSess.run(decWithPastInputs)

                    val curLogitsTensor = stepResult.get("logits").get() as OnnxTensor

                    @Suppress("UNCHECKED_CAST")
                    val curArray3D = curLogitsTensor.value as Array<Array<FloatArray>>
                    val stepLogits = curArray3D[0][0]

                    var maxL = -Float.MAX_VALUE
                    var maxT = 0L
                    for (v in 0 until vocabSize) {
                        if (v == 60715) continue // Suppress <pad> token during generation
                        val logit = stepLogits[v]
                        if (logit > maxL) {
                            maxL = logit
                            maxT = v.toLong()
                        }
                    }

                    if (step <= 5) {
                        System.err.println("[MarianMTEngine] Step $step: inputToken=$lastTokenId -> nextToken=$maxT maxL=$maxL")
                    }

                    val nextDecoderKeyValues = mutableMapOf<String, OnnxTensor>()
                    for (i in 0 until 6) {
                        val decKey = stepResult.get("present.$i.decoder.key").get() as OnnxTensor
                        val decVal = stepResult.get("present.$i.decoder.value").get() as OnnxTensor
                        nextDecoderKeyValues["past_key_values.$i.decoder.key"] = copyOnnxTensor(env, decKey)
                        nextDecoderKeyValues["past_key_values.$i.decoder.value"] = copyOnnxTensor(env, decVal)
                    }

                    curInputIdsTensor.close()
                    stepResult.close()

                    // Close previous step's decoder key values
                    decoderKeyValues.values.forEach { try { it.close() } catch (e: Exception) {} }
                    decoderKeyValues = nextDecoderKeyValues

                    lastTokenId = maxT

                    if (lastTokenId != 0L) {
                        generatedTokenIds.add(lastTokenId)
                    }
                }
            }

            encoderKeyValues.values.forEach { try { it.close() } catch (e: Exception) {} }
            decoderKeyValues.values.forEach { try { it.close() } catch (e: Exception) {} }
        } else {
            // Fallback Full Sequence Decoder Path
            var currentDecoderTokens = mutableListOf(60715L)

            for (step in 0 until maxNewTokens) {
                val decSeqLen = currentDecoderTokens.size.toLong()
                val decShape = longArrayOf(1, decSeqLen)
                val decInputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(currentDecoderTokens.toLongArray()), decShape)

                val decoderInputs = mapOf(
                    "input_ids" to decInputIdsTensor,
                    "encoder_hidden_states" to encoderHiddenStateValue,
                    "encoder_attention_mask" to attentionMaskTensor
                )

                val decoderResult = decoderSess.run(decoderInputs)
                val logitsValue = decoderResult.get("logits").get()
                val logitsTensor = logitsValue as OnnxTensor

                @Suppress("UNCHECKED_CAST")
                val logitsData = logitsTensor.value as Array<Array<FloatArray>>
                val lastLogits = logitsData[0][(decSeqLen - 1).toInt()]
                
                var maxLogit = -Float.MAX_VALUE
                var maxTokenId = 0L

                for (v in 0 until vocabSize) {
                    if (v == 60715) continue // Suppress <pad> token during generation
                    val logit = lastLogits[v]
                    if (logit > maxLogit) {
                        maxLogit = logit
                        maxTokenId = v.toLong()
                    }
                }

                decInputIdsTensor.close()
                decoderResult.close()

                if (maxTokenId == 0L) { // EOS token
                    break
                }

                generatedTokenIds.add(maxTokenId)
                currentDecoderTokens.add(maxTokenId)
            }
        }

        val decoderEndTime = System.currentTimeMillis()
        val decoderTimeMs = decoderEndTime - decoderStartTime

        // Clean up Tensors
        inputIdsTensor.close()
        attentionMaskTensor.close()
        encoderResult.close()

        val totalEndTime = System.currentTimeMillis()
        val totalTimeMs = totalEndTime - totalStartTime

        // 4. Detokenize output token IDs to English
        System.err.println("[MarianMTEngine] Generated Token IDs: ${generatedTokenIds.joinToString(", ")}")
        val englishTranslation = tokenizer.detokenize(generatedTokenIds)
        val peakMemoryKb = Debug.getNativeHeapAllocatedSize() / 1024

        return TranslationResult(
            translatedText = if (englishTranslation.isEmpty()) "[Empty Translation]" else englishTranslation,
            inputTokenCount = inputTokens.size,
            outputTokenCount = generatedTokenIds.size,
            encoderTimeMs = encoderTimeMs,
            decoderTimeMs = decoderTimeMs,
            totalTimeMs = totalTimeMs,
            peakMemoryKb = peakMemoryKb
        )
    }

    override fun unload() {
        try {
            encoderSession?.close()
            decoderSession?.close()
            decoderWithPastSession?.close()
            ortEnv?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            encoderSession = null
            decoderSession = null
            decoderWithPastSession = null
            ortEnv = null
            isLoaded = false
        }
    }
}
