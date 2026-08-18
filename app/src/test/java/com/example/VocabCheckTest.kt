package com.example

import com.example.translation.SentencePieceTokenizer
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VocabCheckTest {
    @Test
    fun checkVocabSpecialTokens() {
        val vocabFile = File("/tmp/test_marian/vocab.json")
        val jsonText = vocabFile.readText()
        val tokenizer = SentencePieceTokenizer()
        tokenizer.load(vocabFile)
        val jaText = "今日は工場の生産ラインを確認して、問題があれば改善します。"
        val tokens = tokenizer.tokenize(jaText)
        println("Tokenized tokens (${tokens.size}): ${tokens.joinToString(", ")}")
        val jsonObject = JSONObject(jsonText)

        println("=== SPECIAL TOKENS IN VOCAB ===")
        val specialCandidates = listOf("</s>", "<s>", "<pad>", "<unk>", "0", "1", "60715", "60714")
        for (cand in specialCandidates) {
            if (jsonObject.has(cand)) {
                println("Key: '$cand' -> Value: ${jsonObject.get(cand)}")
            }
        }

        // Also reverse check IDs 0, 1, 60714, 60715
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val id = jsonObject.getLong(key)
            if (id in listOf(0L, 1L, 2L, 60714L, 60715L)) {
                println("ID: $id -> Token: '$key'")
            }
        }
    }
}
