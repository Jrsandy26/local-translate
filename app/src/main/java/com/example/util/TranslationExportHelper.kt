package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.Language
import com.example.model.RecentTranslation
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String, val description: String) {
    PDF("PDF Document", "pdf", "application/pdf", "Formatted printable document with visual styling"),
    AUDIO("Audio Recording", "m4a", "audio/mp4", "Original recorded microphone audio file (.m4a) from this session"),
    SRT("SRT Subtitles", "srt", "application/x-subrip", "Standard video subtitle format for Premiere, VLC, YouTube"),
    VTT("WebVTT Subtitles", "vtt", "text/vtt", "Web subtitle format for HTML5 video players & online editors"),
    TXT("Plain Text", "txt", "text/plain", "Clean timestamped text transcript"),
    CSV("CSV Spreadsheet", "csv", "text/csv", "Comma-separated table with speakers, source, and translation")
}

enum class SubtitleContentMode(val label: String) {
    BILINGUAL("Dual (Source & Translated)"),
    TRANSLATED_ONLY("Translated Only"),
    SOURCE_ONLY("Source Only")
}

object TranslationExportHelper {

    private fun getExportDir(context: Context): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return exportDir
    }

    /**
     * Generates a subtitle string in SRT format
     */
    fun generateSrt(
        segments: List<TranscriptSegment>,
        contentMode: SubtitleContentMode = SubtitleContentMode.BILINGUAL,
        defaultSegmentDurationSec: Int = 3
    ): String {
        val sb = StringBuilder()
        var currentMs = 0

        segments.forEachIndexed { index, seg ->
            val segDurationMs = if (seg.timestampMs > 0 && index < segments.size - 1) {
                val nextTs = segments[index + 1].timestampMs
                if (nextTs > seg.timestampMs) (nextTs - seg.timestampMs).toInt().coerceIn(1500, 8000)
                else defaultSegmentDurationSec * 1000
            } else {
                defaultSegmentDurationSec * 1000
            }

            val startMs = if (seg.timestampMs > 0) seg.timestampMs.toInt() else currentMs
            val endMs = startMs + segDurationMs
            currentMs = endMs

            val startTimeFormatted = formatTimeSrt(startMs)
            val endTimeFormatted = formatTimeSrt(endMs)

            sb.append("${index + 1}\n")
            sb.append("$startTimeFormatted --> $endTimeFormatted\n")

            when (contentMode) {
                SubtitleContentMode.BILINGUAL -> {
                    sb.append("${seg.translatedText}\n")
                    sb.append("(${seg.sourceText})\n")
                }
                SubtitleContentMode.TRANSLATED_ONLY -> {
                    sb.append("${seg.translatedText}\n")
                }
                SubtitleContentMode.SOURCE_ONLY -> {
                    sb.append("${seg.sourceText}\n")
                }
            }
            sb.append("\n")
        }

        return sb.toString().trimEnd()
    }

    /**
     * Generates a subtitle string in WebVTT format
     */
    fun generateVtt(
        title: String,
        segments: List<TranscriptSegment>,
        contentMode: SubtitleContentMode = SubtitleContentMode.BILINGUAL,
        defaultSegmentDurationSec: Int = 3
    ): String {
        val sb = StringBuilder()
        sb.append("WEBVTT - $title\n\n")
        var currentMs = 0

        segments.forEachIndexed { index, seg ->
            val segDurationMs = if (seg.timestampMs > 0 && index < segments.size - 1) {
                val nextTs = segments[index + 1].timestampMs
                if (nextTs > seg.timestampMs) (nextTs - seg.timestampMs).toInt().coerceIn(1500, 8000)
                else defaultSegmentDurationSec * 1000
            } else {
                defaultSegmentDurationSec * 1000
            }

            val startMs = if (seg.timestampMs > 0) seg.timestampMs.toInt() else currentMs
            val endMs = startMs + segDurationMs
            currentMs = endMs

            val startTimeFormatted = formatTimeVtt(startMs)
            val endTimeFormatted = formatTimeVtt(endMs)

            sb.append("$startTimeFormatted --> $endTimeFormatted\n")

            when (contentMode) {
                SubtitleContentMode.BILINGUAL -> {
                    sb.append("${seg.translatedText}\n")
                    sb.append("<c.dim>(${seg.sourceText})</c>\n")
                }
                SubtitleContentMode.TRANSLATED_ONLY -> {
                    sb.append("${seg.translatedText}\n")
                }
                SubtitleContentMode.SOURCE_ONLY -> {
                    sb.append("${seg.sourceText}\n")
                }
            }
            sb.append("\n")
        }

        return sb.toString().trimEnd()
    }

    /**
     * Generates formatted Plain Text transcript
     */
    fun generatePlainText(
        session: TranslationSession,
        segments: List<TranscriptSegment>
    ): String {
        val srcLang = Language.findByCode(session.sourceLanguageCode)
        val tgtLang = Language.findByCode(session.targetLanguageCode)
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(session.createdAt))

        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("TRANSLATION TRANSCRIPT: ${session.title}\n")
        sb.append("========================================\n")
        sb.append("Date: $dateStr\n")
        sb.append("Languages: ${srcLang.name} (${srcLang.code}) -> ${tgtLang.name} (${tgtLang.code})\n")
        sb.append("Duration: ${session.durationSeconds}s | Segments: ${segments.size}\n")
        sb.append("========================================\n\n")

        segments.forEachIndexed { i, seg ->
            val timestamp = formatTimeShort((i * 3000))
            sb.append("[$timestamp] ${seg.speaker}:\n")
            sb.append("  Original:   ${seg.sourceText}\n")
            sb.append("  Translated: ${seg.translatedText}\n\n")
        }

        return sb.toString().trimEnd()
    }

    /**
     * Generates CSV format for spreadsheets
     */
    fun generateCsv(
        session: TranslationSession,
        segments: List<TranscriptSegment>
    ): String {
        val sb = StringBuilder()
        sb.append("Index,Timestamp,Speaker,Source Text,Translated Text\n")

        segments.forEachIndexed { i, seg ->
            val timestamp = formatTimeShort(i * 3000)
            val cleanSpeaker = escapeCsv(seg.speaker)
            val cleanSrc = escapeCsv(seg.sourceText)
            val cleanTgt = escapeCsv(seg.translatedText)
            sb.append("${i + 1},\"$timestamp\",\"$cleanSpeaker\",\"$cleanSrc\",\"$cleanTgt\"\n")
        }

        return sb.toString()
    }

    /**
     * Generates a styled PDF document
     */
    fun generatePdf(
        context: Context,
        session: TranslationSession,
        segments: List<TranscriptSegment>
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width (72 dpi)
        val pageHeight = 842 // A4 standard height (72 dpi)
        val margin = 40f
        val contentWidth = (pageWidth - (margin * 2)).toInt()

        val srcLang = Language.findByCode(session.sourceLanguageCode)
        val tgtLang = Language.findByCode(session.targetLanguageCode)
        val dateStr = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(session.createdAt))

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPos = margin

        // Paints
        val brandPaint = Paint().apply {
            color = Color.parseColor("#EE7931")
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#FFF4EC")
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#EADCCE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#2C1D13")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = TextPaint().apply {
            color = Color.parseColor("#7E6554")
            textSize = 10.5f
            isAntiAlias = true
        }

        val badgeTextPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val sourceTextPaint = TextPaint().apply {
            color = Color.parseColor("#5A4638")
            textSize = 11.5f
            isAntiAlias = true
        }

        val targetTextPaint = TextPaint().apply {
            color = Color.parseColor("#1E130B")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#FDFBF7")
            isAntiAlias = true
        }

        val orangeBadgePaint = Paint().apply {
            color = Color.parseColor("#EE7931")
            isAntiAlias = true
        }

        fun drawHeader() {
            // Header Card
            val headerHeight = 75f
            val headerRect = RectF(margin, yPos, margin + contentWidth, yPos + headerHeight)
            canvas.drawRoundRect(headerRect, 12f, 12f, headerBgPaint)
            canvas.drawRoundRect(headerRect, 12f, 12f, borderPaint)

            // Brand strip
            val stripRect = RectF(margin, yPos, margin + 6f, yPos + headerHeight)
            canvas.drawRoundRect(stripRect, 3f, 3f, brandPaint)

            // Title
            canvas.drawText("Riva Translate • ${session.title}", margin + 18f, yPos + 26f, titlePaint)

            // Subtitle info
            val infoLine = "Date: $dateStr  |  Duration: ${session.durationSeconds}s  |  Segments: ${segments.size}"
            canvas.drawText(infoLine, margin + 18f, yPos + 44f, metaPaint)

            val langLine = "From: ${srcLang.name} (${srcLang.code}) ➔ To: ${tgtLang.name} (${tgtLang.code})"
            canvas.drawText(langLine, margin + 18f, yPos + 60f, metaPaint)

            yPos += headerHeight + 18f
        }

        drawHeader()

        // Render Segments
        segments.forEachIndexed { index, seg ->
            val timestamp = formatTimeShort(index * 3000)

            // Measure texts using StaticLayout
            val cardPadding = 12f
            val innerWidth = contentWidth - (cardPadding * 2).toInt()

            val srcLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(seg.sourceText, 0, seg.sourceText.length, sourceTextPaint, innerWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.15f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(seg.sourceText, sourceTextPaint, innerWidth, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
            }

            val tgtLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(seg.translatedText, 0, seg.translatedText.length, targetTextPaint, innerWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.15f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(seg.translatedText, targetTextPaint, innerWidth, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
            }

            val itemCardHeight = cardPadding * 2 + 18f + srcLayout.height + 8f + tgtLayout.height

            // Check if we need a new page
            if (yPos + itemCardHeight > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = margin
                drawHeader()
            }

            // Draw Segment Card
            val cardRect = RectF(margin, yPos, margin + contentWidth, yPos + itemCardHeight)
            canvas.drawRoundRect(cardRect, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(cardRect, 10f, 10f, borderPaint)

            // Badge for index & timestamp
            val badgeText = "#${index + 1} • $timestamp"
            val badgeWidth = badgeTextPaint.measureText(badgeText) + 12f
            val badgeRect = RectF(margin + cardPadding, yPos + cardPadding, margin + cardPadding + badgeWidth, yPos + cardPadding + 14f)
            canvas.drawRoundRect(badgeRect, 4f, 4f, orangeBadgePaint)
            canvas.drawText(badgeText, margin + cardPadding + 6f, yPos + cardPadding + 10.5f, badgeTextPaint)

            // Speaker
            canvas.drawText(seg.speaker, margin + cardPadding + badgeWidth + 8f, yPos + cardPadding + 11f, metaPaint)

            var textCursorY = yPos + cardPadding + 22f

            // Source Text
            canvas.save()
            canvas.translate(margin + cardPadding, textCursorY)
            srcLayout.draw(canvas)
            canvas.restore()

            textCursorY += srcLayout.height + 6f

            // Translated Text
            canvas.save()
            canvas.translate(margin + cardPadding, textCursorY)
            tgtLayout.draw(canvas)
            canvas.restore()

            yPos += itemCardHeight + 10f
        }

        // Draw Footer on last page
        val footerText = "Generated by Riva Translate on $dateStr"
        canvas.drawText(footerText, margin, pageHeight - margin + 12f, metaPaint)

        pdfDocument.finishPage(page)

        // Save PDF to file
        val cleanTitle = session.title.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")
        val outputFile = File(getExportDir(context), "${cleanTitle}_${session.id}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    /**
     * Creates and returns a File for any of the export formats
     */
    fun createExportFile(
        context: Context,
        session: TranslationSession,
        segments: List<TranscriptSegment>,
        format: ExportFormat,
        subtitleMode: SubtitleContentMode = SubtitleContentMode.BILINGUAL
    ): File {
        val cleanTitle = session.title.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")
        val fileName = "${cleanTitle}_${session.id}.${format.extension}"
        val file = File(getExportDir(context), fileName)

        when (format) {
            ExportFormat.PDF -> {
                return generatePdf(context, session, segments)
            }
            ExportFormat.AUDIO -> {
                val audioSrc = session.audioFilePath?.let { File(it) }
                if (audioSrc != null && audioSrc.exists() && audioSrc.length() > 0) {
                    audioSrc.copyTo(file, overwrite = true)
                } else {
                    // Create an empty or placeholder audio marker if not present
                    file.writeBytes(byteArrayOf())
                }
            }
            ExportFormat.SRT -> {
                val content = generateSrt(segments, subtitleMode)
                file.writeText(content)
            }
            ExportFormat.VTT -> {
                val content = generateVtt(session.title, segments, subtitleMode)
                file.writeText(content)
            }
            ExportFormat.TXT -> {
                val content = generatePlainText(session, segments)
                file.writeText(content)
            }
            ExportFormat.CSV -> {
                val content = generateCsv(session, segments)
                file.writeText(content)
            }
        }
        return file
    }

    /**
     * Directly shares an audio file recording if it exists
     */
    fun shareAudioDirectly(context: Context, audioPath: String?, sessionTitle: String) {
        if (audioPath.isNullOrBlank()) {
            return
        }
        val audioFile = File(audioPath)
        if (!audioFile.exists() || audioFile.length() == 0L) {
            return
        }
        shareExportedFile(
            context = context,
            file = audioFile,
            mimeType = "audio/mp4",
            title = "Audio Recording: $sessionTitle"
        )
    }

    /**
     * Launches Android Share/Save sheet for the exported file
     */
    fun shareExportedFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TITLE, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export: $title").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("TranslationExport", "Error sharing file", e)
        }
    }

    /**
     * Exports bulk history records (translations) to TXT or CSV
     */
    fun generateBulkTranslationsText(translations: List<RecentTranslation>): String {
        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("RIVA TRANSLATE - TRANSLATION HISTORY (${translations.size})\n")
        sb.append("========================================\n\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        translations.forEachIndexed { idx, item ->
            val dateStr = sdf.format(Date(item.timestamp))
            sb.append("${idx + 1}. [${item.sourceLangCode.uppercase()} -> ${item.targetLangCode.uppercase()}] ($dateStr)\n")
            sb.append("   Source:     ${item.sourceText}\n")
            sb.append("   Translated: ${item.translatedText}\n\n")
        }
        return sb.toString()
    }

    fun generateBulkTranslationsCsv(translations: List<RecentTranslation>): String {
        val sb = StringBuilder()
        sb.append("Index,Date,Source Lang,Target Lang,Source Text,Translated Text,Favorite\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        translations.forEachIndexed { idx, item ->
            val dateStr = sdf.format(Date(item.timestamp))
            val cleanSrc = escapeCsv(item.sourceText)
            val cleanTgt = escapeCsv(item.translatedText)
            sb.append("${idx + 1},\"$dateStr\",\"${item.sourceLangCode}\",\"${item.targetLangCode}\",\"$cleanSrc\",\"$cleanTgt\",\"${item.isFavorite}\"\n")
        }
        return sb.toString()
    }

    private fun formatTimeSrt(ms: Int): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    private fun formatTimeVtt(ms: Int): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }

    private fun formatTimeShort(ms: Int): String {
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }
}
