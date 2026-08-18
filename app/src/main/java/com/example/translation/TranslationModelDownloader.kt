package com.example.translation

import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object TranslationModelDownloader {
    private const val TAG = "TranslationModelDownloader"

    fun downloadMarianMTModel(targetDir: File, progressListener: (Double) -> Unit): Boolean {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val filesToDownload = mapOf(
            "vocab.json" to "https://huggingface.co/onnx-community/opus-mt-ja-en/resolve/main/vocab.json",
            "encoder_model_quantized.onnx" to "https://huggingface.co/onnx-community/opus-mt-ja-en/resolve/main/onnx/encoder_model_quantized.onnx",
            "decoder_model_quantized.onnx" to "https://huggingface.co/onnx-community/opus-mt-ja-en/resolve/main/onnx/decoder_model_quantized.onnx"
        )

        val totalFiles = filesToDownload.size
        var completedFiles = 0

        for ((fileName, urlStr) in filesToDownload) {
            val destinationFile = File(targetDir, fileName)
            if (destinationFile.exists() && destinationFile.length() > 0) {
                completedFiles++
                progressListener(completedFiles.toDouble() / totalFiles)
                continue
            }

            Log.i(TAG, "Downloading $fileName from $urlStr")
            val success = downloadFile(urlStr, destinationFile) { fileProgress ->
                val overallProgress = (completedFiles + fileProgress) / totalFiles
                progressListener(overallProgress)
            }
            if (!success) {
                Log.e(TAG, "Failed to download $fileName")
                return false
            }
            completedFiles++
        }

        return true
    }

    private fun downloadFile(urlStr: String, destinationFile: File, progress: (Double) -> Unit): Boolean {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.e(TAG, "Server returned response code $responseCode for URL: $urlStr")
                return false
            }

            val fileLength = connection.contentLengthLong
            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(destinationFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    progress(total.toDouble() / fileLength)
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: $urlStr", e)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            return false
        } finally {
            connection?.disconnect()
        }
    }
}
