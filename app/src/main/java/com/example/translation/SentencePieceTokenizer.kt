package com.example.translation

import android.util.Log
import org.json.JSONObject
import java.io.File

class SentencePieceTokenizer {
    private val vocabMap = mutableMapOf<String, Long>()
    private val idMap = mutableMapOf<Long, String>()

    fun load(vocabFile: File): Boolean {
        try {
            if (!vocabFile.exists()) return false
            val text = vocabFile.readText()
            val json = JSONObject(text)
            vocabMap.clear()
            idMap.clear()
            for (key in json.keys()) {
                val id = json.getLong(key)
                vocabMap[key] = id
                idMap[id] = key
            }
            Log.i("SentencePieceTokenizer", "Loaded vocab size: ${vocabMap.size}")
            return true
        } catch (e: Exception) {
            Log.e("SentencePieceTokenizer", "Failed to load vocab", e)
            return false
        }
    }

    fun tokenize(text: String): LongArray {
        if (text.isEmpty()) return LongArray(0)
        
        // SentencePiece prepends " " (Unicode U+2581) to represent spaces
        val normalized = " " + text.replace(" ", " ")
        val tokens = mutableListOf<Long>()
        var i = 0
        while (i < normalized.length) {
            var match: String? = null
            var matchId: Long? = null
            // Try matching longest sub-string starting from i
            for (len in Math.min(20, normalized.length - i) downTo 1) {
                val substr = normalized.substring(i, i + len)
                val id = vocabMap[substr]
                if (id != null) {
                    match = substr
                    matchId = id
                    break
                }
            }
            if (matchId != null && match != null) {
                tokens.add(matchId)
                i += match.length
            } else {
                // Fallback to <unk> or skip character
                val unkId = vocabMap["<unk>"] ?: 2L
                tokens.add(unkId)
                i++
            }
        }
        
        // Add EOS token at the end </s>
        val eosId = vocabMap["</s>"] ?: 0L
        tokens.add(eosId)
        
        return tokens.toLongArray()
    }

    fun detokenize(tokenIds: List<Long>): String {
        val sb = StringBuilder()
        for (id in tokenIds) {
            if (id == 0L || id == 1L || id == 60715L) { // </s>, <s>, <pad>
                continue
            }
            val piece = idMap[id] ?: continue
            sb.append(piece)
        }
        // Clean up SentencePiece spaces
        return sb.toString().replace(" ", " ").replace(Regex("\\s+"), " ").trim()
    }
}
