package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.model.TranscriptSegment
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.QuickSpeechInputDialog
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.theme.TextGlassSubtitle
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun FaceToFaceScreen(viewModel: TranslationViewModel) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val showLanguageSheet by viewModel.showLanguageSheet.collectAsState()
    val isSelectingSourceLanguage by viewModel.isSelectingSourceLanguage.collectAsState()

    var isChatViewMode by remember { mutableStateOf(false) }
    var showSpeechDialogSpeaker by remember { mutableStateOf<Int?>(null) }

    val segments = remember(currentSession) {
        currentSession?.getSortedSegments() ?: emptyList()
    }

    val speaker1Segments = remember(segments) {
        segments.filter { it.speaker.contains("1") || it.speaker.contains("Host", ignoreCase = true) }
    }
    val speaker2Segments = remember(segments) {
        segments.filter { it.speaker.contains("2") || it.speaker.contains("Guest", ignoreCase = true) }
    }

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        size = 36.dp,
                        onClick = {
                            viewModel.stopLiveListening()
                            viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME)
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Real-Time Conversation",
                        color = TextGlassHeading,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // View Mode Toggle Button
                GlassIconButton(
                    icon = Icons.Default.Chat,
                    contentDescription = "Toggle View Mode",
                    size = 36.dp,
                    onClick = { isChatViewMode = !isChatViewMode }
                )
            }

            // Language Selector Bar: [ Source Lang ▾ ]  ⇄  [ Target Lang ▾ ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x20FFFFFF))
                        .clickable {
                            viewModel.isSelectingSourceLanguage.value = true
                            viewModel.showLanguageSheet.value = true
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "Speaker 1: ${sourceLang.name}", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(10.dp))

                GlassIconButton(
                    icon = Icons.Default.SwapHoriz,
                    contentDescription = "Swap",
                    size = 32.dp,
                    onClick = { viewModel.swapLanguages() }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x20FFFFFF))
                        .clickable {
                            viewModel.isSelectingSourceLanguage.value = false
                            viewModel.showLanguageSheet.value = true
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "Speaker 2: ${targetLang.name}", color = Color(0xFFC084FC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isChatViewMode) {
                // CHAT STREAM VIEW MODE
                ConversationChatStream(
                    segments = segments,
                    sourceLangName = sourceLang.name,
                    targetLangName = targetLang.name,
                    onSpeakSegment = { viewModel.speakSegment(it) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            } else {
                // FACE-TO-FACE SPLIT SCREEN MODE
                Column(modifier = Modifier.weight(1f)) {
                    // Top Half: Speaker 2 (Guest - Rotated 180° for opposite person)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .graphicsLayer { rotationZ = 180f }
                            .padding(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxSize(),
                            isActive = speaker2Segments.isNotEmpty()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${targetLang.name} (Guest)",
                                        color = Color(0xFFC084FC),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val lastSeg2 = speaker2Segments.lastOrNull()
                                    if (lastSeg2 != null) {
                                        GlassIconButton(
                                            icon = Icons.Default.VolumeUp,
                                            contentDescription = "Speak",
                                            size = 28.dp,
                                            onClick = { viewModel.speakSegment(lastSeg2) }
                                        )
                                    }
                                }

                                val lastSeg2 = speaker2Segments.lastOrNull()
                                Column(modifier = Modifier.weight(1f)) {
                                    if (lastSeg2 == null) {
                                        Text(
                                            text = "Tap Speak for Guest (${targetLang.name})...",
                                            color = TextGlassMuted,
                                            fontSize = 15.sp
                                        )
                                    } else {
                                        Text(
                                            text = lastSeg2.sourceText,
                                            color = TextGlassHeading,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = lastSeg2.translatedText,
                                            color = NeonEmerald,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    MicIconButton(
                                        onClick = { showSpeechDialogSpeaker = 2 },
                                        color = Color(0xFFC084FC),
                                        modifier = Modifier.testTag("speaker2_mic_button")
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Half: Speaker 1 (Host)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxSize(),
                            isActive = speaker1Segments.isNotEmpty()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${sourceLang.name} (Host)",
                                        color = NeonCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val lastSeg1 = speaker1Segments.lastOrNull()
                                    if (lastSeg1 != null) {
                                        GlassIconButton(
                                            icon = Icons.Default.VolumeUp,
                                            contentDescription = "Speak",
                                            size = 28.dp,
                                            onClick = { viewModel.speakSegment(lastSeg1) }
                                        )
                                    }
                                }

                                val lastSeg1 = speaker1Segments.lastOrNull()
                                Column(modifier = Modifier.weight(1f)) {
                                    if (lastSeg1 == null) {
                                        Text(
                                            text = "Tap Speak for Host (${sourceLang.name})...",
                                            color = TextGlassMuted,
                                            fontSize = 15.sp
                                        )
                                    } else {
                                        Text(
                                            text = lastSeg1.sourceText,
                                            color = TextGlassHeading,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = lastSeg1.translatedText,
                                            color = NeonEmerald,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    MicIconButton(
                                        onClick = { showSpeechDialogSpeaker = 1 },
                                        color = NeonCyan,
                                        modifier = Modifier.testTag("speaker1_mic_button")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Session Bar: Main Session Listening / Control Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(if (isListening) Color(0xFFEF4444).copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, if (isListening) Color(0xFFEF4444) else NeonCyan, RoundedCornerShape(30.dp))
                        .clickable { viewModel.toggleLiveListening() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Pause else Icons.Default.Mic,
                            contentDescription = "Toggle Conversation Listening",
                            tint = if (isListening) Color(0xFFEF4444) else NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "Pause Conversation" else "Start Live Session",
                            color = TextGlassHeading,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal Speech Dialog
    showSpeechDialogSpeaker?.let { speakerNum ->
        QuickSpeechInputDialog(
            onDismiss = { showSpeechDialogSpeaker = null },
            onTextSubmitted = { text, _ ->
                val speakerLabel = if (speakerNum == 1) "Speaker 1" else "Speaker 2"
                viewModel.submitManualUtterance(text, speakerLabel)
                showSpeechDialogSpeaker = null
            }
        )
    }

    // Language Selector Sheet
    if (showLanguageSheet) {
        val currentCode = if (isSelectingSourceLanguage) sourceLang.code else targetLang.code
        LanguageSelectorSheet(
            title = if (isSelectingSourceLanguage) "Speaker 1 Language" else "Speaker 2 Language",
            currentSelectedCode = currentCode,
            onLanguageSelected = { lang ->
                if (isSelectingSourceLanguage) {
                    viewModel.setSourceLanguage(lang)
                } else {
                    viewModel.setTargetLanguage(lang)
                }
                viewModel.showLanguageSheet.value = false
            },
            onDismiss = { viewModel.showLanguageSheet.value = false }
        )
    }
}

@Composable
fun ConversationChatStream(
    segments: List<TranscriptSegment>,
    sourceLangName: String,
    targetLangName: String,
    onSpeakSegment: (TranscriptSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(segments.size) {
        if (segments.isNotEmpty()) {
            listState.animateScrollToItem(segments.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(segments) { segment ->
            val isSpeaker1 = segment.speaker.contains("1") || segment.speaker.contains("Host", ignoreCase = true)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isSpeaker1) Arrangement.Start else Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSpeaker1) Color(0x2500F0FF) else Color(0x25C084FC))
                        .border(
                            1.dp,
                            if (isSpeaker1) NeonCyan.copy(alpha = 0.4f) else Color(0xFFC084FC).copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSpeaker1) "Speaker 1 ($sourceLangName)" else "Speaker 2 ($targetLangName)",
                                color = if (isSpeaker1) NeonCyan else Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            GlassIconButton(
                                icon = Icons.Default.VolumeUp,
                                contentDescription = "Listen",
                                size = 28.dp,
                                onClick = { onSpeakSegment(segment) }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = segment.sourceText,
                            color = TextGlassHeading,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = segment.translatedText,
                            color = NeonEmerald,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MicIconButton(
    onClick: () -> Unit,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .border(1.5.dp, color, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Speak",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
