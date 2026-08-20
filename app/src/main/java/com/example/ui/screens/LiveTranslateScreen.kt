package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.model.Language
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import com.example.ui.components.ExportSessionDialog
import com.example.ui.components.SessionPlaybackDialog
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.TranslationViewModel

// Dynamic Live Translate color palette adapting to Light & Dark themes
private val ScreenBackground @Composable get() = AppTheme.liveTheme.screenBackground
private val CardBg @Composable get() = AppTheme.liveTheme.cardBackground
private val PillBg @Composable get() = AppTheme.liveTheme.pillBackground
private val PillBorder @Composable get() = AppTheme.liveTheme.pillBorder
private val DarkBrownText @Composable get() = AppTheme.liveTheme.titleText
private val SubtitleBrownText @Composable get() = AppTheme.liveTheme.subtitleText
private val WarmOrange @Composable get() = AppTheme.liveTheme.accentOrange
private val WarmOrangeLight @Composable get() = AppTheme.liveTheme.accentOrangeLight
private val ControlCardBg @Composable get() = AppTheme.liveTheme.controlCardBg
private val ControlCircleBg @Composable get() = AppTheme.liveTheme.controlCircleBg
private val StopRed = Color(0xFFD32F2F)
private val LiveRed = Color(0xFFE65100)
private val WaveformOrange @Composable get() = AppTheme.liveTheme.accentOrange
private val DottedLineColor @Composable get() = AppTheme.liveTheme.dottedLine


@Composable
fun LiveTranslateScreen(
    viewModel: TranslationViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSessionRunning by viewModel.isLiveSessionRunning.collectAsState()
    val isSessionPaused by viewModel.isLiveSessionPaused.collectAsState()
    val timerSeconds by viewModel.liveTimerSeconds.collectAsState()
    val recordAudioChecked by viewModel.recordAudioChecked.collectAsState()
    val segments by viewModel.liveSegments.collectAsState()
    val partialText by viewModel.livePartialText.collectAsState()
    val partialTranslated by viewModel.livePartialTranslated.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()

    val showStoppedDialog by viewModel.showSessionStoppedDialog.collectAsState()
    val completedSession by viewModel.completedSession.collectAsState()
    val completedSegments by viewModel.completedSegments.collectAsState()

    var showLiveExportDialog by remember { mutableStateOf(false) }

    if (showLiveExportDialog && segments.isNotEmpty()) {
        val tempSession = remember(segments, timerSeconds, sourceLang, targetLang) {
            TranslationSession(
                id = System.currentTimeMillis(),
                title = "Live ${sourceLang.name} to ${targetLang.name}",
                sourceLanguageCode = sourceLang.code,
                targetLanguageCode = targetLang.code,
                durationSeconds = timerSeconds.coerceAtLeast(segments.size * 3),
                createdAt = System.currentTimeMillis()
            )
        }
        ExportSessionDialog(
            session = tempSession,
            segments = segments,
            onDismiss = { showLiveExportDialog = false }
        )
    }

    // Treat as active view if session running or if we have conversation items
    val isActiveMode = isSessionRunning || segments.isNotEmpty() || partialText.isNotEmpty()

    if (showStoppedDialog && completedSession != null) {
        SessionPlaybackDialog(
            session = completedSession,
            segments = completedSegments,
            viewModel = viewModel,
            onDismiss = {
                viewModel.showSessionStoppedDialog.value = false
            },
            onStartNewSession = {
                viewModel.showSessionStoppedDialog.value = false
                viewModel.resetLiveTranscript()
                viewModel.startLiveSession()
            },
            onGoToHistory = {
                viewModel.showSessionStoppedDialog.value = false
                viewModel.setActiveScreen(ActiveScreen.HISTORY)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
    ) {
        // 1. Top App Bar
        TopLiveBar(
            onBack = onBack,
            onHistoryClick = { viewModel.setActiveScreen(ActiveScreen.HISTORY) },
            onToggleTheme = { viewModel.toggleThemeMode() },
            isDarkTheme = AppTheme.liveTheme.isDark
        )


        // 2. Language Selector Row (Japanese ⇄ English)
        LanguageBar(
            sourceLang = sourceLang,
            targetLang = targetLang,
            onSourceClick = {
                viewModel.isSelectingSource.value = true
                viewModel.showLanguageSelector.value = true
            },
            onTargetClick = {
                viewModel.isSelectingSource.value = false
                viewModel.showLanguageSelector.value = true
            },
            onSwapClick = { viewModel.swapLanguages() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Main Center Card (Active Live Transcript OR Idle "Tap the mic to start")
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            AnimatedContent(
                targetState = isActiveMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "LiveModeContent"
            ) { active ->
                if (active) {
                    ActiveLiveTranscriptView(
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                        segments = segments,
                        partialText = partialText,
                        partialTranslated = partialTranslated,
                        isPaused = isSessionPaused,
                        onExportClick = { showLiveExportDialog = true }
                    )
                } else {
                    IdleLiveTranslateView(
                        recordAudioChecked = recordAudioChecked,
                        onRecordAudioToggle = { viewModel.toggleRecordAudio() },
                        onStartClick = { viewModel.startLiveSession() },
                        rmsLevel = rmsLevel
                    )
                }
            }
        }

        // 4. Bottom Control Panel (When Active)
        if (isActiveMode) {
            ActiveBottomControls(
                timerSeconds = timerSeconds,
                isPaused = isSessionPaused,
                onStopClick = { viewModel.stopLiveSession() },
                onPauseClick = {
                    if (isSessionPaused) {
                        viewModel.resumeLiveSession()
                    } else {
                        viewModel.pauseLiveSession()
                    }
                },
                onResumeClick = { viewModel.resumeLiveSession() }
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun TopLiveBar(
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .testTag("live_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkBrownText,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Live Translate",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBrownText
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("live_theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                    tint = DarkBrownText,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("live_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Translation History",
                    tint = DarkBrownText,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}


@Composable
private fun LanguageBar(
    sourceLang: Language,
    targetLang: Language,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Source Language Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(PillBg)
                .clickable { onSourceClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("live_source_lang_pill"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = sourceLang.flag, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = sourceLang.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkBrownText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = DarkBrownText,
                modifier = Modifier.size(20.dp)
            )
        }

        // Swap Icon
        IconButton(
            onClick = onSwapClick,
            modifier = Modifier
                .size(40.dp)
                .testTag("live_swap_lang_button")
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = DarkBrownText,
                modifier = Modifier.size(24.dp)
            )
        }

        // Target Language Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(PillBg)
                .clickable { onTargetClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("live_target_lang_pill"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = targetLang.flag, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = targetLang.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkBrownText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = DarkBrownText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun IdleLiveTranslateView(
    recordAudioChecked: Boolean,
    onRecordAudioToggle: () -> Unit,
    onStartClick: () -> Unit,
    rmsLevel: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section with Speaking Illustration and Explanatory Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Custom Head & Soundwaves Illustration
            SpeakingIllustration(modifier = Modifier.size(160.dp))

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Tap the mic to start",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrownText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Start speaking and we'll\ntranslate in real time",
                fontSize = 15.sp,
                color = SubtitleBrownText,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Record audio checkbox pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PillBg)
                    .clickable { onRecordAudioToggle() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("record_audio_checkbox"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (recordAudioChecked) WarmOrange else Color.Transparent)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (recordAudioChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Record audio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkBrownText
                )
            }
        }

        // Bottom section with Waveform matrix and Large Mic button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Audio Dots Matrix
                AudioDotMatrix(isLeft = true)

                Spacer(modifier = Modifier.width(20.dp))

                // Center Large Warm Orange Mic Button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(WarmOrangeLight)
                        .clickable { onStartClick() }
                        .padding(10.dp)
                        .testTag("start_live_mic_button"),
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
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Start Speaking",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Right Audio Dots Matrix
                AudioDotMatrix(isLeft = false)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tap to start",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = SubtitleBrownText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ActiveLiveTranscriptView(
    sourceLang: Language,
    targetLang: Language,
    segments: List<TranscriptSegment>,
    partialText: String,
    partialTranslated: String,
    isPaused: Boolean,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top status & export row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (segments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PillBg)
                        .clickable { onExportClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Export Subtitles & PDF",
                        tint = WarmOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export Subtitles",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBrownText
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Top "● Live" badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "LiveAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) Color(0xFF9E9E9E) else LiveRed.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isPaused) "Paused" else "Live",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPaused) Color(0xFF757575) else LiveRed
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transcript Items
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(segments) { item ->
                TranscriptBubbleItem(
                    sourceLangTag = sourceLang.nativeName,
                    sourceText = item.sourceText,
                    targetLangTag = targetLang.name,
                    targetText = item.translatedText
                )
            }

            // Real-time streaming speech item
            if (partialText.isNotBlank()) {
                item {
                    TranscriptBubbleItem(
                        sourceLangTag = sourceLang.nativeName,
                        sourceText = partialText,
                        targetLangTag = targetLang.name,
                        targetText = if (partialTranslated.isNotBlank()) partialTranslated else "..."
                    )
                }
            }
        }
    }
}

@Composable
private fun TranscriptBubbleItem(
    sourceLangTag: String,
    sourceText: String,
    targetLangTag: String,
    targetText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Source Language Tag Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(PillBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = sourceLangTag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SubtitleBrownText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Source Text (Japanese / Original)
        Text(
            text = sourceText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = DarkBrownText
        )

        // Dotted Divider with Audio Waveform blip in the middle
        DottedWaveformDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Target Language Tag Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(PillBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = targetLangTag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SubtitleBrownText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Target Text (English / Translated - Bold)
        Text(
            text = targetText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBrownText
        )
    }
}

@Composable
private fun ActiveBottomControls(
    timerSeconds: Int,
    isPaused: Boolean,
    onStopClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ControlCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 18.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Row (Red Dot + mm:ss)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) Color(0xFF9E9E9E) else LiveRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val minutes = timerSeconds / 60
                val seconds = timerSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkBrownText
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3 Control Buttons: Stop, Pause/Play, Resume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Stop Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ControlCircleBg)
                            .clickable { onStopClick() }
                            .testTag("live_stop_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(StopRed)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stop",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBrownText
                    )
                }

                // 2. Pause / Play Button (Center Big)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(WarmOrangeLight)
                            .clickable { onPauseClick() }
                            .padding(4.dp)
                            .testTag("live_pause_button"),
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
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) "Resume" else "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBrownText
                    )
                }

                // 3. Resume Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ControlCircleBg)
                            .clickable { onResumeClick() }
                            .testTag("live_resume_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = if (isPaused) WarmOrange else Color(0xFFB5A496),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Resume",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBrownText
                    )
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing the Head Profile and Soundwaves Illustration with Star Sparkles
 */
@Composable
private fun SpeakingIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw Star Sparkles around
        drawSparkle(Offset(w * 0.22f, h * 0.28f), size = 7f, color = Color(0xFFF7BD99))
        drawSparkle(Offset(w * 0.72f, h * 0.26f), size = 8f, color = Color(0xFFF7BD99))
        drawSparkle(Offset(w * 0.21f, h * 0.58f), size = 6f, color = Color(0xFFF7BD99))
        drawSparkle(Offset(w * 0.76f, h * 0.55f), size = 7f, color = Color(0xFFF7BD99))

        // Small decorative dots
        drawCircle(Color(0xFFFAD1B8), radius = 3.5f, center = Offset(w * 0.32f, h * 0.22f))
        drawCircle(Color(0xFFFAD1B8), radius = 3f, center = Offset(w * 0.70f, h * 0.68f))

        // 2. Draw Speaking Head Silhouette (Facing Right)
        val headColor = Color(0xFFF7CFB3)
        val headPath = Path().apply {
            // Start at back base of neck
            moveTo(w * 0.36f, h * 0.78f)
            // Back neck to back of skull
            lineTo(w * 0.36f, h * 0.50f)
            cubicTo(
                w * 0.32f, h * 0.40f,
                w * 0.34f, h * 0.24f,
                w * 0.46f, h * 0.24f
            )
            // Top of head to forehead
            cubicTo(
                w * 0.54f, h * 0.24f,
                w * 0.58f, h * 0.28f,
                w * 0.58f, h * 0.36f
            )
            // Nose bridge and tip
            lineTo(w * 0.58f, h * 0.42f)
            lineTo(w * 0.63f, h * 0.45f)
            lineTo(w * 0.59f, h * 0.47f)
            // Upper lip, open mouth, chin
            lineTo(w * 0.60f, h * 0.50f)
            lineTo(w * 0.56f, h * 0.52f) // mouth opening
            lineTo(w * 0.60f, h * 0.55f) // lower lip & chin
            cubicTo(
                w * 0.58f, h * 0.61f,
                w * 0.54f, h * 0.64f,
                w * 0.50f, h * 0.66f
            )
            // Front of neck down
            lineTo(w * 0.49f, h * 0.78f)
            close()
        }
        drawPath(headPath, color = headColor)

        // 3. Soundwaves radiating from mouth )))
        val waveColor = Color(0xFFE87A38)
        val mouthX = w * 0.62f
        val mouthY = h * 0.52f

        // Wave 1
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(mouthX - 4f, mouthY - 14f),
            size = Size(28f, 28f),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // Wave 2
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(mouthX + 10f, mouthY - 24f),
            size = Size(48f, 48f),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticBezierTo(center.x, center.y, center.x + size, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + size)
        quadraticBezierTo(center.x, center.y, center.x - size, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color = color)
}

/**
 * Audio Dot Matrix shown on the left and right of the main mic button in Idle state
 */
@Composable
private fun AudioDotMatrix(isLeft: Boolean) {
    Canvas(modifier = Modifier.size(width = 64.dp, height = 36.dp)) {
        val cols = 5
        val dotRadius = 2.2f
        val spacingX = size.width / (cols + 1)
        val dotColor = Color(0xFFE8B69B)

        val heightPattern = if (isLeft) {
            listOf(2, 3, 5, 4, 3)
        } else {
            listOf(3, 4, 5, 3, 2)
        }

        for (col in 0 until cols) {
            val numDots = heightPattern[col]
            val x = (col + 1) * spacingX
            val startY = (size.height - (numDots * 6.5f)) / 2f

            for (row in 0 until numDots) {
                val y = startY + row * 6.5f
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Dotted line with waveform blip in the middle, exactly matching Screenshot 1
 */
@Composable
private fun DottedWaveformDivider(modifier: Modifier = Modifier) {
    val dotColor = DottedLineColor
    val waveColor = WaveformOrange

    Canvas(modifier = modifier.height(20.dp)) {
        val y = size.height / 2f
        val totalWidth = size.width
        val dotRadius = 1.6f
        val dotSpacing = 8f
        val waveformCenter = totalWidth * 0.65f
        val waveformWidth = 44f

        // Draw Dotted line excluding waveform region
        var x = 0f
        while (x < totalWidth) {
            if (x < waveformCenter - waveformWidth / 2 || x > waveformCenter + waveformWidth / 2) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
            x += dotSpacing
        }

        // Draw orange waveform vertical blip bars in the middle
        val barHeights = listOf(6f, 14f, 22f, 12f, 5f)
        val barWidth = 3f
        val barSpacing = 6f
        val startWaveX = waveformCenter - (barHeights.size * barSpacing) / 2f

        barHeights.forEachIndexed { index, barH ->
            val bx = startWaveX + index * barSpacing
            drawRoundRect(
                color = waveColor,
                topLeft = Offset(bx, y - barH / 2f),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
        }
    }
}
