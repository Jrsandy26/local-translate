package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.model.ExportFormat
import com.example.model.Language
import com.example.model.SessionWithSegments
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExportManager {

    fun exportAndShare(
        context: Context,
        sessionWithSegments: SessionWithSegments,
        format: ExportFormat,
        includeTimestamps: Boolean = true,
        includeSourceText: Boolean = true
    ): Result<File> {
        return try {
            val session = sessionWithSegments.session
            val sanitizedTitle = session.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${sanitizedTitle}_$timestamp.${format.extension}"

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)

            when (format) {
                ExportFormat.TEXT -> generateTextFile(file, sessionWithSegments, includeTimestamps, includeSourceText)
                ExportFormat.WORD -> generateWordFile(file, sessionWithSegments, includeTimestamps, includeSourceText)
                ExportFormat.PDF -> generatePdfFile(file, sessionWithSegments, includeTimestamps, includeSourceText)
            }

            shareFile(context, file, format.mimeType, session.title)
            Result.success(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun generateTextFile(
        file: File,
        data: SessionWithSegments,
        includeTimestamps: Boolean,
        includeSourceText: Boolean
    ) {
        val session = data.session
        val segments = data.getSortedSegments()
        val srcLang = Language.findByCode(session.sourceLanguageCode)
        val tgtLang = Language.findByCode(session.targetLanguageCode)
        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(session.timestamp))
        val durationStr = String.format("%02d:%02d", session.durationSeconds / 60, session.durationSeconds % 60)

        val sb = StringBuilder()
        sb.append("=====================================================\n")
        sb.append(" LIVE TRANSLATION TRANSCRIPT REPORT\n")
        sb.append("=====================================================\n")
        sb.append("Session Title : ${session.title}\n")
        sb.append("Date & Time   : $dateStr\n")
        sb.append("Language Pair : ${srcLang.name} (${srcLang.flag}) -> ${tgtLang.name} (${tgtLang.flag})\n")
        sb.append("Duration      : $durationStr\n")
        sb.append("Privacy Mode  : 100% Offline On-Device Secure\n")
        sb.append("=====================================================\n\n")

        segments.forEach { segment ->
            if (includeTimestamps) {
                sb.append("[${segment.getFormattedShortTimestamp()}] ${segment.speaker}\n")
            }
            if (includeSourceText) {
                sb.append("Source (${srcLang.name}):\n")
                sb.append("${segment.sourceText}\n\n")
            }
            sb.append("Translation (${tgtLang.name}):\n")
            sb.append("${segment.translatedText}\n")
            sb.append("-----------------------------------------------------\n\n")
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
    }

    private fun generateWordFile(
        file: File,
        data: SessionWithSegments,
        includeTimestamps: Boolean,
        includeSourceText: Boolean
    ) {
        val session = data.session
        val segments = data.getSortedSegments()
        val srcLang = Language.findByCode(session.sourceLanguageCode)
        val tgtLang = Language.findByCode(session.targetLanguageCode)
        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(session.timestamp))
        val durationStr = String.format("%02d:%02d", session.durationSeconds / 60, session.durationSeconds % 60)

        val html = buildString {
            append("<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>")
            append("<head><meta charset='utf-8'><title>${session.title}</title>")
            append("<style>")
            append("body { font-family: Calibri, 'Segoe UI', sans-serif; font-size: 11pt; color: #1a1a1a; margin: 30px; }")
            append(".header { background-color: #0b132b; color: #ffffff; padding: 18px; border-radius: 8px; margin-bottom: 20px; }")
            append(".header h1 { margin: 0 0 8px 0; font-size: 18pt; color: #00c2ff; }")
            append(".meta-table { width: 100%; border-collapse: collapse; margin-top: 10px; }")
            append(".meta-table td { padding: 4px 8px; color: #e0e0e0; font-size: 10pt; }")
            append(".entry { margin-bottom: 16px; padding: 12px; background-color: #f8f9fa; border-left: 4px solid #007aff; border-radius: 4px; }")
            append(".time-tag { font-weight: bold; color: #007aff; font-size: 10pt; margin-bottom: 6px; }")
            append(".source-text { font-size: 11pt; color: #333333; margin-bottom: 6px; line-height: 1.4; }")
            append(".trans-text { font-size: 11.5pt; color: #000000; font-weight: bold; line-height: 1.4; }")
            append(".footer { margin-top: 30px; font-size: 9pt; color: #888888; text-align: center; border-top: 1px solid #ddd; padding-top: 10px; }")
            append("</style></head><body>")

            append("<div class='header'>")
            append("<h1>${session.title}</h1>")
            append("<table class='meta-table'>")
            append("<tr><td><strong>Date:</strong> $dateStr</td><td><strong>Duration:</strong> $durationStr</td></tr>")
            append("<tr><td><strong>Languages:</strong> ${srcLang.name} (${srcLang.code}) &rarr; ${tgtLang.name} (${tgtLang.code})</td><td><strong>Security:</strong> 100% Offline Local Processing</td></tr>")
            append("</table>")
            append("</div>")

            segments.forEach { segment ->
                append("<div class='entry'>")
                if (includeTimestamps) {
                    append("<div class='time-tag'>&#9201; ${segment.getFormattedShortTimestamp()} &bull; ${segment.speaker}</div>")
                }
                if (includeSourceText) {
                    append("<div class='source-text'>${segment.sourceText}</div>")
                }
                append("<div class='trans-text'>${segment.translatedText}</div>")
                append("</div>")
            }

            append("<div class='footer'>Generated securely by Live Translate App &bull; Confidential</div>")
            append("</body></html>")
        }

        file.writeText(html, Charsets.UTF_8)
    }

    private fun generatePdfFile(
        file: File,
        data: SessionWithSegments,
        includeTimestamps: Boolean,
        includeSourceText: Boolean
    ) {
        val document = PdfDocument()
        val session = data.session
        val segments = data.getSortedSegments()
        val srcLang = Language.findByCode(session.sourceLanguageCode)
        val tgtLang = Language.findByCode(session.targetLanguageCode)
        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(session.timestamp))
        val durationStr = String.format("%02d:%02d", session.durationSeconds / 60, session.durationSeconds % 60)

        val pageWidth = 595 // Standard A4 width in points (72 dpi)
        val pageHeight = 842 // Standard A4 height in points
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val titlePaint = TextPaint().apply {
            color = Color.rgb(10, 132, 255)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = TextPaint().apply {
            color = Color.rgb(90, 90, 95)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val timestampPaint = TextPaint().apply {
            color = Color.rgb(0, 122, 255)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val sourcePaint = TextPaint().apply {
            color = Color.rgb(60, 60, 67)
            textSize = 11f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val transPaint = TextPaint().apply {
            color = Color.rgb(18, 18, 18)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(245, 246, 248)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardBorderPaint = Paint().apply {
            color = Color.rgb(220, 224, 230)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val accentBarPaint = Paint().apply {
            color = Color.rgb(10, 132, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw header on first page
        var currentY = 40f
        canvas.drawText("LIVE TRANSLATION REPORT", 40f, currentY, titlePaint)
        currentY += 18f
        canvas.drawText("${session.title}  •  $dateStr  •  Duration: $durationStr", 40f, currentY, metaPaint)
        currentY += 14f
        canvas.drawText("Languages: ${srcLang.name} (${srcLang.code}) → ${tgtLang.name} (${tgtLang.code})  •  100% Offline On-Device", 40f, currentY, metaPaint)
        currentY += 24f

        val margin = 40f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        for (segment in segments) {
            // Measure segment block height
            val tsLayout = if (includeTimestamps) {
                StaticLayout.Builder.obtain(
                    "${segment.getFormattedShortTimestamp()}  •  ${segment.speaker}",
                    0,
                    "${segment.getFormattedShortTimestamp()}  •  ${segment.speaker}".length,
                    timestampPaint,
                    contentWidth - 30
                ).build()
            } else null

            val srcLayout = if (includeSourceText) {
                StaticLayout.Builder.obtain(
                    segment.sourceText,
                    0,
                    segment.sourceText.length,
                    sourcePaint,
                    contentWidth - 30
                ).build()
            } else null

            val transLayout = StaticLayout.Builder.obtain(
                segment.translatedText,
                0,
                segment.translatedText.length,
                transPaint,
                contentWidth - 30
            ).build()

            var blockHeight = 24f + transLayout.height.toFloat()
            if (tsLayout != null) blockHeight += tsLayout.height + 6f
            if (srcLayout != null) blockHeight += srcLayout.height + 8f

            // Check page overflow
            if (currentY + blockHeight > pageHeight - 50f) {
                // Finish page
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            // Draw Card background
            val cardRect = RectF(margin, currentY, margin + contentWidth, currentY + blockHeight)
            canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(cardRect, 8f, 8f, cardBorderPaint)
            canvas.drawRoundRect(RectF(margin, currentY, margin + 4f, currentY + blockHeight), 4f, 4f, accentBarPaint)

            var innerY = currentY + 12f
            if (tsLayout != null) {
                canvas.save()
                canvas.translate(margin + 16f, innerY)
                tsLayout.draw(canvas)
                canvas.restore()
                innerY += tsLayout.height + 6f
            }

            if (srcLayout != null) {
                canvas.save()
                canvas.translate(margin + 16f, innerY)
                srcLayout.draw(canvas)
                canvas.restore()
                innerY += srcLayout.height + 8f
            }

            canvas.save()
            canvas.translate(margin + 16f, innerY)
            transLayout.draw(canvas)
            canvas.restore()

            currentY += blockHeight + 14f
        }

        document.finishPage(page)

        val outputStream = FileOutputStream(file)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Transcript: $title")
            putExtra(Intent.EXTRA_TEXT, "Here is the exported bilingual transcript for $title.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Export & Share Transcript").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
