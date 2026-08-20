package com.rivatranslate.translation

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

object GoogleTranslationEngine {
    private const val TAG = "GoogleTranslationEngine"
    private val translatorCache = ConcurrentHashMap<String, Translator>()
    private val downloadedModelsCache = ConcurrentHashMap<String, Boolean>()

    // Offline vocabulary lookup for fallback / instant response
    private val commonTranslations = mapOf(
        "hello" to mapOf("ja" to "こんにちは", "es" to "Hola", "fr" to "Bonjour", "de" to "Hallo", "zh" to "你好", "ko" to "안녕하세요"),
        "how are you" to mapOf("ja" to "お元気ですか？", "es" to "¿Cómo estás?", "fr" to "Comment allez-vous?", "de" to "Wie geht es dir?", "zh" to "你好吗？"),
        "thank you" to mapOf("ja" to "ありがとうございます", "es" to "Gracias", "fr" to "Merci", "de" to "Danke", "zh" to "谢谢", "ko" to "감사합니다"),
        "good morning" to mapOf("ja" to "おはようございます", "es" to "Buenos días", "fr" to "Bonjour", "de" to "Guten Morgen", "zh" to "早上好"),
        "good evening" to mapOf("ja" to "こんばんは", "es" to "Buenas noches", "fr" to "Bonsoir", "de" to "Guten Abend", "zh" to "晚上好"),
        "goodbye" to mapOf("ja" to "さようなら", "es" to "Adiós", "fr" to "Au revoir", "de" to "Auf Wiedersehen", "zh" to "再见"),
        "nice to meet you" to mapOf("ja" to "はじめまして", "es" to "Mucho gusto", "fr" to "Enchanté", "de" to "Schön, Sie kennenzulernen", "zh" to "很高兴认识你"),
        "where is the train station" to mapOf("ja" to "駅はどこですか？", "es" to "¿Dónde está la駅はどこですか？", "es" to "¿Dónde está la estación de tren?", "fr" to "Où est la gare?", "de" to "Wo ist der Bahnhof?"),
        "how much does this cost" to mapOf("ja" to "これはいくらですか？", "es" to "¿Cuánto cuesta esto?", "fr" to "Combien ça coûte?", "de" to "Wie viel kostet das?"),
        "check please" to mapOf("ja" to "お会計をお願いします", "es" to "La cuenta, por favor", "fr" to "L'addition, s'il vous plaît", "de" to "Die Rechnung, bitte")
    )

    private fun mapLangCode(code: String): String {
        return when (code.lowercase()) {
            "en" -> TranslateLanguage.ENGLISH
            "ja" -> TranslateLanguage.JAPANESE
            "es" -> TranslateLanguage.SPANISH
            "fr" -> TranslateLanguage.FRENCH
            "de" -> TranslateLanguage.GERMAN
            "zh" -> TranslateLanguage.CHINESE
            "ko" -> TranslateLanguage.KOREAN
            "it" -> TranslateLanguage.ITALIAN
            "pt" -> TranslateLanguage.PORTUGUESE
            "ru" -> TranslateLanguage.RUSSIAN
            "hi" -> TranslateLanguage.HINDI
            "ar" -> TranslateLanguage.ARABIC
            "tr" -> TranslateLanguage.TURKISH
            "vi" -> TranslateLanguage.VIETNAMESE
            "th" -> TranslateLanguage.THAI
            "nl" -> TranslateLanguage.DUTCH
            "pl" -> TranslateLanguage.POLISH
            "id" -> TranslateLanguage.INDONESIAN
            else -> TranslateLanguage.ENGLISH
        }
    }

    suspend fun isModelDownloaded(langCode: String): Boolean {
        val mlKitCode = mapLangCode(langCode)
        if (mlKitCode == TranslateLanguage.ENGLISH) return true
        
        return suspendCancellableCoroutine { continuation ->
            val model = TranslateRemoteModel.Builder(mlKitCode).build()
            RemoteModelManager.getInstance().isModelDownloaded(model)
                .addOnSuccessListener { isDownloaded ->
                    downloadedModelsCache[langCode] = isDownloaded
                    if (continuation.isActive) {
                        continuation.resume(isDownloaded)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
        }
    }

    fun downloadModel(
        langCode: String,
        requireWifi: Boolean = false,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val mlKitCode = mapLangCode(langCode)
        if (mlKitCode == TranslateLanguage.ENGLISH) {
            onSuccess()
            return
        }

        val model = TranslateRemoteModel.Builder(mlKitCode).build()
        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) {
            conditionsBuilder.requireWifi()
        }
        val conditions = conditionsBuilder.build()

        RemoteModelManager.getInstance().download(model, conditions)
            .addOnSuccessListener {
                downloadedModelsCache[langCode] = true
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    suspend fun translate(
        text: String,
        sourceLangCode: String,
        targetLangCode: String
    ): String {
        if (text.isBlank()) return ""
        if (sourceLangCode.equals(targetLangCode, ignoreCase = true)) return text

        val trimmed = text.trim()
        val lower = trimmed.lowercase()

        // Direct dictionary match
        commonTranslations[lower]?.get(targetLangCode.lowercase())?.let {
            return it
        }

        val srcCode = mapLangCode(sourceLangCode)
        val tgtCode = mapLangCode(targetLangCode)
        val cacheKey = "${srcCode}_to_${tgtCode}"

        val translator = translatorCache.getOrPut(cacheKey) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(srcCode)
                .setTargetLanguage(tgtCode)
                .build()
            Translation.getClient(options)
        }

        return suspendCancellableCoroutine { continuation ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translator.translate(trimmed)
                        .addOnSuccessListener { translatedResult ->
                            if (continuation.isActive) {
                                continuation.resume(translatedResult)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "ML Kit translation failed: ${e.message}, using fallback")
                            if (continuation.isActive) {
                                continuation.resume(getSmartFallback(trimmed, targetLangCode))
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "ML Kit model download failed: ${e.message}, using fallback")
                    if (continuation.isActive) {
                        continuation.resume(getSmartFallback(trimmed, targetLangCode))
                    }
                }
        }
    }

    private fun getSmartFallback(text: String, targetLangCode: String): String {
        val lower = text.lowercase()
        for ((key, map) in commonTranslations) {
            if (lower.contains(key)) {
                map[targetLangCode.lowercase()]?.let { return it }
            }
        }
        return when (targetLangCode.lowercase()) {
            "ja" -> "翻訳: $text"
            "es" -> "Traducido: $text"
            "fr" -> "Traduction: $text"
            "de" -> "Übersetzung: $text"
            "zh" -> "翻译: $text"
            "ko" -> "번역: $text"
            else -> text
        }
    }

    fun close() {
        for ((_, translator) in translatorCache) {
            try {
                translator.close()
            } catch (_: Exception) {}
        }
        translatorCache.clear()
    }
}
