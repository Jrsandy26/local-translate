package com.rivatranslate.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rivatranslate.model.RecentTranslation
import com.rivatranslate.util.TranslationExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val DialogBg = Color(0xFFFFFDF9)
private val WarmOrange = Color(0xFFEE7931)
private val WarmOrangeLight = Color(0xFFFFEAD9)
private val DarkBrown = Color(0xFF2C1D13)
private val SubtitleBrown = Color(0xFF8C7362)
private val PillBg = Color(0xFFF4ECE4)
private val PreviewBg = Color(0xFF1E1E1E)

@Composable
fun BulkExportDialog(
    translations: List<RecentTranslation>,
    title: String = "Export Translations",
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isCsvFormat by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val exportText = remember(translations, isCsvFormat) {
        if (isCsvFormat) {
            TranslationExportHelper.generateBulkTranslationsCsv(translations)
        } else {
            TranslationExportHelper.generateBulkTranslationsText(translations)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("bulk_export_dialog"),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(WarmOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = WarmOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown
                            )
                            Text(
                                text = "${translations.size} records ready to save",
                                fontSize = 12.sp,
                                color = SubtitleBrown
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkBrown)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Toggle (Text vs CSV)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PillBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isCsvFormat) WarmOrange else Color.Transparent)
                            .clickable { isCsvFormat = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Plain Text (.txt)",
                            fontSize = 12.sp,
                            fontWeight = if (!isCsvFormat) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isCsvFormat) Color.White else DarkBrown
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCsvFormat) WarmOrange else Color.Transparent)
                            .clickable { isCsvFormat = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Spreadsheet (.csv)",
                            fontSize = 12.sp,
                            fontWeight = if (isCsvFormat) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCsvFormat) Color.White else DarkBrown
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Title & Copy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "File Content Preview:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBrown
                    )

                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Export Translations", exportText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied content to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 11.sp, color = WarmOrange)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Content Preview Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PreviewBg)
                        .border(1.dp, Color(0xFFE2D6CA), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = exportText,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBrown)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            isExporting = true
                            coroutineScope.launch {
                                val ext = if (isCsvFormat) "csv" else "txt"
                                val mime = if (isCsvFormat) "text/csv" else "text/plain"
                                val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
                                val file = File(exportDir, "translations_history_${System.currentTimeMillis()}.$ext")
                                withContext(Dispatchers.IO) {
                                    file.writeText(exportText)
                                }
                                isExporting = false
                                TranslationExportHelper.shareExportedFile(
                                    context = context,
                                    file = file,
                                    mimeType = mime,
                                    title = "Export Translations ($ext)"
                                )
                            }
                        },
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                        enabled = !isExporting
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Save File")
                    }
                }
            }
        }
    }
}
