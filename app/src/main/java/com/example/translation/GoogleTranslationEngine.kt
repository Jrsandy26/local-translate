package com.example.translation

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

object GoogleTranslationEngine {
    private const val TAG = "GoogleTranslationEngine"
    private val modelManager = RemoteModelManager.getInstance()
    private val translatorCache = ConcurrentHashMap<String, Translator>()

    /**
     * Map language code string to ML Kit TranslateLanguage constant
     */
    fun getMlKitLanguage(code: String): String {
        return TranslateLanguage.fromLanguageTag(code.lowercase()) ?: code.lowercase()
    }

    /**
     * Checks if a Google ML Kit offline model is downloaded for a given language code.
     */
    suspend fun isModelDownloaded(langCode: String): Boolean = withContext(Dispatchers.IO) {
        if (langCode.equals("en", ignoreCase = true)) {
            // Base English model is built-in or lightweight
            return@withContext true
        }
        val mlKitLang = getMlKitLanguage(langCode)
        val model = TranslateRemoteModel.Builder(mlKitLang).build()
        try {
            val task = modelManager.isModelDownloaded(model)
            val isDownloaded = Tasks.await(task)
            isDownloaded
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model download status for $langCode", e)
            false
        }
    }

    /**
     * Downloads Google Offline Translation model for a specific language code.
     */
    suspend fun downloadModel(
        langCode: String,
        requireWifi: Boolean = false,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val mlKitLang = getMlKitLanguage(langCode)
        val model = TranslateRemoteModel.Builder(mlKitLang).build()
        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) {
            conditionsBuilder.requireWifi()
        }
        val conditions = conditionsBuilder.build()

        try {
            val task = modelManager.download(model, conditions)
            Tasks.await(task)
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download model for $langCode", e)
            onFailure(e)
        }
    }

    /**
     * Deletes a downloaded Google Offline Translation model.
     */
    suspend fun deleteModel(langCode: String): Boolean = withContext(Dispatchers.IO) {
        val mlKitLang = getMlKitLanguage(langCode)
        val model = TranslateRemoteModel.Builder(mlKitLang).build()
        try {
            val task = modelManager.deleteDownloadedModel(model)
            Tasks.await(task)
            translatorCache.clear()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete model for $langCode", e)
            false
        }
    }

    /**
     * Translates text using Google ML Kit On-Device Translation Engine.
     */
    suspend fun translate(
        text: String,
        sourceLangCode: String,
        targetLangCode: String
    ): String? = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return@withContext ""
        if (sourceLangCode.equals(targetLangCode, ignoreCase = true)) return@withContext cleanText

        val srcMl = getMlKitLanguage(sourceLangCode)
        val tgtMl = getMlKitLanguage(targetLangCode)
        val cacheKey = "$srcMl->$tgtMl"

        try {
            var translator = translatorCache[cacheKey]
            if (translator == null) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(srcMl)
                    .setTargetLanguage(tgtMl)
                    .build()
                translator = Translation.getClient(options)
                
                // Ensure model is downloaded or download automatically on demand
                val downloadConditions = DownloadConditions.Builder().build()
                val downloadTask = translator.downloadModelIfNeeded(downloadConditions)
                Tasks.await(downloadTask)
                
                translatorCache[cacheKey] = translator
            }

            val translateTask = translator.translate(cleanText)
            Tasks.await(translateTask)
        } catch (e: Exception) {
            Log.w(TAG, "Google ML Kit translation fallback for $sourceLangCode -> $targetLangCode", e)
            null
        }
    }

    fun close() {
        translatorCache.values.forEach { 
            try { it.close() } catch (_: Exception) {} 
        }
        translatorCache.clear()
    }
}
