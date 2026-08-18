package com.example.tts

import android.util.Log
import org.json.JSONObject
import java.io.File

class PiperPhonemizer {
    private val phonemeIdMap = mutableMapOf<String, IntArray>()

    fun load(configFile: File): Boolean {
        try {
            if (!configFile.exists()) return false
            val text = configFile.readText()
            val json = JSONObject(text)
            
            val mapObj = json.optJSONObject("phoneme_id_map") 
                ?: json.optJSONObject("id_map")
                
            phonemeIdMap.clear()
            if (mapObj != null) {
                for (key in mapObj.keys()) {
                    val arr = mapObj.getJSONArray(key)
                    val intArr = IntArray(arr.length())
                    for (i in 0 until arr.length()) {
                        intArr[i] = arr.getInt(i)
                    }
                    phonemeIdMap[key] = intArr
                }
            }
            Log.i("PiperPhonemizer", "Loaded phoneme ID map with size: ${phonemeIdMap.size}")
            return true
        } catch (e: Exception) {
            Log.e("PiperPhonemizer", "Failed to load phoneme config", e)
            return false
        }
    }

    fun textToPhonemeIds(text: String): LongArray {
        val ids = mutableListOf<Long>()
        // Pad beginning with space/silence if needed
        ids.add(0L)
        
        for (char in text.lowercase()) {
            val s = char.toString()
            val mapped = phonemeIdMap[s]
            if (mapped != null) {
                for (id in mapped) {
                    ids.add(id.toLong())
                }
            } else {
                val code = char.code
                if (code in 0..255) {
                    ids.add(code.toLong())
                }
            }
            ids.add(0L) // Separator
        }
        
        ids.add(0L) // End padding
        return ids.toLongArray()
    }
}
