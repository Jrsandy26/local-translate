package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import com.example.ui.viewmodel.TranslationViewModel
import java.text.SimpleDateFormat
import java.util.*

private val DialogBg = Color(0xFFFFFDF9)
private val CardInnerBg = Color(0xFFFFF6EE)
private val WarmOrange = Color(0xFFEE7931)
private val WarmOrangeLight = Color(0xFFFFEAD9)
private val DarkBrown = Color(0xFF2C1D13)
private val SubtitleBrown = Color(0xFF8C7362)
private val PillBg = Color(0xFFF4ECE4)
private val HighlightBorder = Color(0xFFEE7931)
private val ActiveCardBg = Color(0xFFFFF0E0)

@Composable
fun SessionPlaybackDialog(
    session: TranslationSession?,
    segments: List<TranscriptSegment>,
    viewModel: TranslationViewModel,
    onDismiss: () -> Unit,
    onStartNewSession: (() -> Unit)? = null,
    onGoToHistory: (() -> Unit)? = null,
    onDeleteSession: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (session == null) return

    val context = LocalContext.current
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
    val currentPosMs by viewModel.audioPlaybackPosMs.collectAsState()
    val totalDurationMs by viewModel.audioPlaybackDurationMs.collectAsState()
    val activeSegmentIdx by viewModel.activePlaybackSegmentIndex.collectAsState()

    var playModeTranslated by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog) {
        ExportSessionDialog(
            session = session,
            segments = segments,
            onDismiss = { showExportDialog = false }
        )
    }

    val effectiveDurationMs = if (totalDurationMs > 0) {
        totalDurationMs
    } else {
        (session.durationSeconds * 1000).coerceAtLeast(segments.size * 3000)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudioPlayback()
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.stopAudioPlayback()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("session_playback_dialog"),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with checkmark & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Session Stopped",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown
                            )
                            val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(session.createdAt))
                            Text(
                                text = "$dateStr • ${session.durationSeconds}s • ${segments.size} lines",
                                fontSize = 12.sp,
                                color = SubtitleBrown
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WarmOrangeLight)
                                .testTag("session_export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Subtitles & PDF",
                                tint = WarmOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                viewModel.stopAudioPlayback()
                                onDismiss()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DarkBrown
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Player Control Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardInnerBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title / Mode Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkBrown
                            )

                            // Toggle Mode Chip
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PillBg)
                                    .clickable {
                                        playModeTranslated = !playModeTranslated
                                        viewModel.stopAudioPlayback()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (playModeTranslated) Icons.Default.Translate else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = WarmOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (playModeTranslated) "Voice (TTS)" else "Recording",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkBrown
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Slider
                        val progressFraction = if (effectiveDurationMs > 0) {
                            (currentPosMs.toFloat() / effectiveDurationMs.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        Slider(
                            value = progressFraction,
                            onValueChange = { frac ->
                                val targetPos = (frac * effectiveDurationMs).toInt()
                                viewModel.seekAudioPlayback(targetPos)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = WarmOrange,
                                activeTrackColor = WarmOrange,
                                inactiveTrackColor = Color(0xFFE2D4C6)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Time stamps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val curSec = (currentPosMs / 1000)
                            val totSec = (effectiveDurationMs / 1000)
                            Text(
                                text = String.format("%02d:%02d", curSec / 60, curSec % 60),
                                fontSize = 12.sp,
                                color = SubtitleBrown
                            )
                            Text(
                                text = String.format("%02d:%02d", totSec / 60, totSec % 60),
                                fontSize = 12.sp,
                                color = SubtitleBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Playback Control Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stop / Reset
                            IconButton(
                                onClick = { viewModel.stopAudioPlayback() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PillBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = DarkBrown,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Main Play/Pause Button
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(WarmOrangeLight)
                                    .clickable {
                                        if (isAudioPlaying) {
                                            viewModel.pauseAudioPlayback()
                                        } else {
                                            if (playModeTranslated) {
                                                viewModel.playSequentialTts(
                                                    segments = segments,
                                                    useTranslated = true,
                                                    targetLangCode = session.targetLanguageCode
                                                )
                                            } else {
                                                viewModel.playSessionAudio(
                                                    audioPath = session.audioFilePath,
                                                    segments = segments,
                                                    targetLangCode = session.targetLanguageCode
                                                )
                                            }
                                        }
                                    }
                                    .padding(4.dp)
                                    .testTag("dialog_play_pause_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(WarmOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isAudioPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Replay from beginning
                            IconButton(
                                onClick = {
                                    viewModel.stopAudioPlayback()
                                    if (playModeTranslated) {
                                        viewModel.playSequentialTts(
                                            segments = segments,
                                            useTranslated = true,
                                            targetLangCode = session.targetLanguageCode
                                        )
                                    } else {
                                        viewModel.playSessionAudio(
                                            audioPath = session.audioFilePath,
                                            segments = segments,
                                            targetLangCode = session.targetLanguageCode
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PillBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Replay",
                                    tint = DarkBrown,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transcript Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Synced Transcript (${segments.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBrown
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PillBg)
                                .clickable { showExportDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = WarmOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export Subtitles",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkBrown
                            )
                        }

                        if (onDeleteSession != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onDeleteSession,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Session",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Transcript Segments List
                if (segments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No speech detected in this session",
                            fontSize = 14.sp,
                            color = SubtitleBrown,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(segments) { idx, seg ->
                            val isActive = isAudioPlaying && activeSegmentIdx == idx

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) ActiveCardBg else Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isActive) Modifier.border(
                                            1.5.dp,
                                            HighlightBorder,
                                            RoundedCornerShape(16.dp)
                                        ) else Modifier
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    // Top row: Speaker tag & quick actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PillBg)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = seg.speaker,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = SubtitleBrown
                                            )
                                        }

                                        Row {
                                            // Speak source
                                            IconButton(
                                                onClick = {
                                                    viewModel.playSingleSegment(seg, speakTranslated = false)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Speak Original",
                                                    tint = SubtitleBrown,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Speak translated
                                            IconButton(
                                                onClick = {
                                                    viewModel.playSingleSegment(seg, speakTranslated = true)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.RecordVoiceOver,
                                                    contentDescription = "Speak Translation",
                                                    tint = WarmOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Copy
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("Translation", "${seg.sourceText}\n${seg.translatedText}")
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = SubtitleBrown,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Source Text
                                    Text(
                                        text = seg.sourceText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = DarkBrown
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Translated Text (Bold)
                                    Text(
                                        text = seg.translatedText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBrown
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onStartNewSession != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.stopAudioPlayback()
                                onStartNewSession()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = WarmOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Session", fontSize = 13.sp)
                        }
                    }

                    if (onGoToHistory != null) {
                        Button(
                            onClick = {
                                viewModel.stopAudioPlayback()
                                onGoToHistory()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View History", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
