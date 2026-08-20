package com.rivatranslate.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rivatranslate.model.TranscriptSegment
import com.rivatranslate.model.TranslationSession
import com.rivatranslate.util.ExportFormat
import com.rivatranslate.util.SubtitleContentMode
import com.rivatranslate.util.TranslationExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val DialogBg = Color(0xFFFFFDF9)
private val CardInnerBg = Color(0xFFFFF6EE)
private val WarmOrange = Color(0xFFEE7931)
private val WarmOrangeLight = Color(0xFFFFEAD9)
private val DarkBrown = Color(0xFF2C1D13)
private val SubtitleBrown = Color(0xFF8C7362)
private val PillBg = Color(0xFFF4ECE4)
private val PreviewBg = Color(0xFF1E1E1E)

@Composable
fun ExportSessionDialog(
    session: TranslationSession,
    segments: List<TranscriptSegment>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var selectedSubtitleMode by remember { mutableStateOf(SubtitleContentMode.BILINGUAL) }
    var isExporting by remember { mutableStateOf(false) }

    // Live preview text for text-based formats
    val previewText = remember(selectedFormat, selectedSubtitleMode, segments) {
        when (selectedFormat) {
            ExportFormat.PDF -> "📄 PDF Document with styled header, language flags, metadata, and boxed transcript cards ready for export & printing."
            ExportFormat.AUDIO -> "🎵 Audio Recording (.m4a AAC audio file from session microphone input)"
            ExportFormat.SRT -> TranslationExportHelper.generateSrt(segments, selectedSubtitleMode)
            ExportFormat.VTT -> TranslationExportHelper.generateVtt(session.title, segments, selectedSubtitleMode)
            ExportFormat.TXT -> TranslationExportHelper.generatePlainText(session, segments)
            ExportFormat.CSV -> TranslationExportHelper.generateCsv(session, segments)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("export_session_dialog"),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WarmOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = WarmOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export Translation",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown
                            )
                            Text(
                                text = "${session.title} • ${segments.size} segments",
                                fontSize = 12.sp,
                                color = SubtitleBrown
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkBrown
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Selector Tabs (PDF, SRT, VTT, TXT, CSV)
                Text(
                    text = "Select Format:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkBrown
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { fmt ->
                        FormatChip(
                            format = fmt,
                            isSelected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle options (for SRT and VTT)
                AnimatedVisibility(visible = selectedFormat == ExportFormat.SRT || selectedFormat == ExportFormat.VTT) {
                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        Text(
                            text = "Subtitle Mode:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubtitleContentMode.values().forEach { mode ->
                                val isSelected = selectedSubtitleMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) WarmOrange else PillBg)
                                        .clickable { selectedSubtitleMode = mode }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else DarkBrown,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Format Description
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardInnerBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (selectedFormat) {
                                ExportFormat.PDF -> Icons.Default.PictureAsPdf
                                ExportFormat.AUDIO -> Icons.Default.Headphones
                                ExportFormat.SRT, ExportFormat.VTT -> Icons.Default.Subtitles
                                ExportFormat.TXT -> Icons.Default.Description
                                ExportFormat.CSV -> Icons.Default.TableChart
                            },
                            contentDescription = null,
                            tint = WarmOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedFormat.description,
                            fontSize = 12.sp,
                            color = DarkBrown
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preview (${selectedFormat.extension.uppercase()}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBrown
                    )

                    if (selectedFormat != ExportFormat.PDF && selectedFormat != ExportFormat.AUDIO) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("${session.title} ${selectedFormat.displayName}", previewText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ${selectedFormat.displayName} to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = WarmOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Content", fontSize = 11.sp, color = WarmOrange)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable preview viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedFormat == ExportFormat.PDF || selectedFormat == ExportFormat.AUDIO) Color(0xFFF9F7F3) else PreviewBg)
                        .border(1.dp, Color(0xFFE2D6CA), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    if (selectedFormat == ExportFormat.PDF) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WarmOrangeLight)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "📄 ${session.title}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBrown
                                    )
                                    Text(
                                        text = "Languages: ${session.sourceLanguageCode.uppercase()} ➔ ${session.targetLanguageCode.uppercase()} • Duration: ${session.durationSeconds}s",
                                        fontSize = 11.sp,
                                        color = SubtitleBrown
                                    )
                                }
                            }

                            segments.take(5).forEachIndexed { i, seg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "#${i + 1} • ${seg.speaker}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmOrange
                                        )
                                        Text(
                                            text = seg.sourceText,
                                            fontSize = 12.sp,
                                            color = Color(0xFF555555)
                                        )
                                        Text(
                                            text = seg.translatedText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            if (segments.size > 5) {
                                Text(
                                    text = "... and ${segments.size - 5} more segments included in final PDF",
                                    fontSize = 11.sp,
                                    color = SubtitleBrown,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    } else if (selectedFormat == ExportFormat.AUDIO) {
                        val audioFile = session.audioFilePath?.let { java.io.File(it) }
                        val fileExists = audioFile != null && audioFile.exists() && audioFile.length() > 0L
                        val fileSizeKb = if (fileExists) (audioFile!!.length() / 1024) else 0L

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(WarmOrangeLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = WarmOrange,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "Session Audio Recording",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown
                            )

                            if (fileExists) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Format:", fontSize = 12.sp, color = SubtitleBrown)
                                            Text("MPEG-4 AAC (.m4a)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkBrown)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Duration:", fontSize = 12.sp, color = SubtitleBrown)
                                            Text("${session.durationSeconds} seconds", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkBrown)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("File Size:", fontSize = 12.sp, color = SubtitleBrown)
                                            Text("${fileSizeKb} KB", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkBrown)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Quality:", fontSize = 12.sp, color = SubtitleBrown)
                                            Text("128 kbps • 44.1 kHz", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkBrown)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "✓ Ready to share directly to WhatsApp, Drive, Files, Email, or other apps.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF2E7D32),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "No Audio File Recorded",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarkBrown
                                        )
                                        Text(
                                            text = "Microphone audio recording was disabled or not saved for this session.\n\nYou can still export formatted PDF, SRT Subtitles, WebVTT, TXT, or CSV transcripts.",
                                            fontSize = 12.sp,
                                            color = SubtitleBrown,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = previewText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE0E0E0),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkBrown
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            isExporting = true
                            coroutineScope.launch {
                                val exportedFile = withContext(Dispatchers.IO) {
                                    TranslationExportHelper.createExportFile(
                                        context = context,
                                        session = session,
                                        segments = segments,
                                        format = selectedFormat,
                                        subtitleMode = selectedSubtitleMode
                                    )
                                }
                                isExporting = false
                                TranslationExportHelper.shareExportedFile(
                                    context = context,
                                    file = exportedFile,
                                    mimeType = selectedFormat.mimeType,
                                    title = "${session.title} (${selectedFormat.displayName})"
                                )
                                Toast.makeText(context, "Export ready: ${exportedFile.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_export_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmOrange
                        ),
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporting...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Save File")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatChip(
    format: ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (format) {
        ExportFormat.PDF -> Icons.Default.PictureAsPdf
        ExportFormat.AUDIO -> Icons.Default.Headphones
        ExportFormat.SRT, ExportFormat.VTT -> Icons.Default.Subtitles
        ExportFormat.TXT -> Icons.Default.Description
        ExportFormat.CSV -> Icons.Default.TableChart
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) WarmOrange else PillBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else DarkBrown,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = format.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else DarkBrown
            )
        }
    }
}
