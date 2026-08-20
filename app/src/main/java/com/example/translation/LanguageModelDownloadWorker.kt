package com.example.translation

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class LanguageModelDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val langCode = inputData.getString(KEY_LANG_CODE) ?: return Result.failure()
        Log.d(TAG, "Starting background download of language model: $langCode")

        try {
            val isDownloaded = GoogleTranslationEngine.isModelDownloaded(langCode)
            if (!isDownloaded) {
                GoogleTranslationEngine.downloadModel(
                    langCode = langCode,
                    requireWifi = false,
                    onSuccess = {
                        Log.i(TAG, "Successfully downloaded model $langCode in background worker")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failure downloading model $langCode in background worker", e)
                    }
                )
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in background worker downloading $langCode", e)
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "ModelDownloadWorker"
        const val KEY_LANG_CODE = "key_lang_code"

        fun enqueueDownload(context: Context, langCode: String) {
            if (langCode.equals("en", ignoreCase = true)) return

            val data = Data.Builder()
                .putString(KEY_LANG_CODE, langCode)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LanguageModelDownloadWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
