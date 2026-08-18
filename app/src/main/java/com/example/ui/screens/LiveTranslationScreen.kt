package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.model.TranscriptSegment
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.QuickSpeechInputDialog
import com.example.ui.viewmodel.TranslationViewModel

// Theme colors strictly matched from the user screenshots
private val MaterialLiveBackgroundTop = Color(0xFF102A54)
private val MaterialLiveBackgroundBottom = Color(0xFF0C2042)
private val MaterialLiveBottomGlow = Color(0xFF1E4680)
private val MaterialLivePillBg = Color(0xFF23416F)
private val MaterialLivePillSelectedBg = Color(0xFF2B4D82)
private val MaterialLiveMutedText = Color(0xFF9FB4CE)
private val MaterialLiveLightText = Color(0xFFCBD5E1)
private val MaterialLivePauseInnerCircle = Color(0xFF6382A8)
private val MaterialLiveRecordDot = Color(0xFFEF4444)
private val MaterialLivePrivacyGreen = Color(0xFF22C55E)

@Composable
fun LiveTranslationScreen(viewModel: TranslationViewModel) {
    val isListening by viewModel.isListening.collectAsState()
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val partialTranscript by viewModel.partialTranscriptText.collectAsState()
    val partialTranslation by viewModel.partialTranslationText.collectAsState()
    val totalDurationSec by viewModel.totalDurationSec.collectAsState()
    val showLanguageSheet by viewModel.showLanguageSheet.collectAsState()
    val isSelectingSourceLanguage by viewModel.isSelectingSourceLanguage.collectAsState()
    val recordAudioEnabled by viewModel.recordAudioEnabled.collectAsState()

    var showManualSpeechDialog by remember { mutableStateOf(false) }

    val segments = remember(currentSession) {
        currentSession?.getSortedSegments() ?: emptyList()
    }

    // Has any speech occurred in this live session?
    val hasSpeech = segments.isNotEmpty() || partialTranscript.isNotEmpty()

    // Determine state:
    // 1. IDLE (not listening and no speech started yet)
    // 2. ACTIVE_LISTENING (listening: waiting for speech OR showing speech)
    // 3. PAUSED_WITH_SPEECH (stopped listening but has speech to review)
    val isIdleState = !isListening && !hasSpeech

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialLiveBackgroundTop,
                        MaterialLiveBackgroundBottom,
                        if (isListening) MaterialLiveBottomGlow.copy(alpha = 0.6f) else MaterialLiveBackgroundBottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Privacy Green Indicator Dot at top right when recording
        if (isListening) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 16.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialLivePrivacyGreen)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // -------------------------------------------------------------
            // TOP BAR (Visible in Idle and Paused states, or subtle in live)
            // -------------------------------------------------------------
            if (isIdleState || !isListening) {
                LiveTopAppBar(
                    onBackClick = {
                        viewModel.stopLiveListening()
                        viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME)
                    },
                    onHistoryClick = {
                        viewModel.stopLiveListening()
                        viewModel.setActiveScreen(ActiveScreen.HISTORY)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selector Pills: [ Japanese ▼ ] ⇄ [ English ▼ ]
                LiveLanguageSelectorRow(
                    sourceLangName = sourceLang.name,
                    targetLangName = targetLang.name,
                    onSourceClick = {
                        viewModel.isSelectingSourceLanguage.value = true
                        viewModel.showLanguageSheet.value = true
                    },
                    onTargetClick = {
                        viewModel.isSelectingSourceLanguage.value = false
                        viewModel.showLanguageSheet.value = true
                    },
                    onSwapClick = { viewModel.swapLanguages() }
                )
            } else {
                // In full immersive recording mode, a minimal top bar with back navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                                onClick = {
                                    viewModel.stopLiveListening()
                                    viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Active language subtitle
                    Text(
                        text = "${sourceLang.name}  ⇄  ${targetLang.name}",
                        color = MaterialLiveMutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Box(modifier = Modifier.size(44.dp)) // Spacer for alignment
                }
            }

            // -------------------------------------------------------------
            // CENTER CONTENT AREA
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // STATE 1: Idle Screen before recording (Screenshot 2)
                    isIdleState -> {
                        IdleCenterIllustration(
                            onIllustrationClick = { showManualSpeechDialog = true }
                        )
                    }

                    // STATE 2: Active Listening but No Speech Yet (Screenshot 1)
                    isListening && !hasSpeech -> {
                        ListeningPromptText()
                    }

                    // STATE 3: Active Listening OR Paused with Recognized/Translated Speech (Screenshot 3)
                    else -> {
                        LiveTranscriptsStream(
                            segments = segments,
                            partialSource = partialTranscript,
                            partialTranslation = partialTranslation,
                            onSpeakSegment = { viewModel.speakSegment(it) },
                            onSpeakSource = { viewModel.speakSource(it) }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // BOTTOM CONTROLS AREA
            // -------------------------------------------------------------
            if (isIdleState) {
                // Screenshot 2 Bottom Controls:
                // [✓] Record audio
                // [Big Mic Button]
                // "Tap to start"
                IdleBottomControls(
                    recordAudioEnabled = recordAudioEnabled,
                    onToggleRecordAudio = {
                        viewModel.recordAudioEnabled.value = !recordAudioEnabled
                    },
                    onMicClick = {
                        viewModel.startLiveListening()
                    },
                    onQuickTestClick = {
                        showManualSpeechDialog = true
                    }
                )
            } else {
                // Screenshot 1 & 3 Bottom Controls:
                // ● 00:01 / ● 00:09 (timer)
                // [Big Circular Pause / Resume Button]
                // "Tap to pause" / "Tap to resume"
                ActiveRecordingBottomControls(
                    isListening = isListening,
                    elapsedSeconds = totalDurationSec,
                    onPauseResumeClick = {
                        viewModel.toggleLiveListening()
                    },
                    onSimulateSpeech = {
                        showManualSpeechDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Sheet for Selecting Language
    if (showLanguageSheet) {
        val currentSelectedCode = if (isSelectingSourceLanguage) sourceLang.code else targetLang.code
        val title = if (isSelectingSourceLanguage) "Translate From" else "Translate To"
        LanguageSelectorSheet(
            title = title,
            currentSelectedCode = currentSelectedCode,
            onLanguageSelected = { lang ->
                if (isSelectingSourceLanguage) {
                    viewModel.setSourceLanguage(lang)
                } else {
                    viewModel.setTargetLanguage(lang)
                }
                viewModel.showLanguageSheet.value = false
            },
            onDismiss = {
                viewModel.showLanguageSheet.value = false
            }
        )
    }

    // Quick speech input dialog (useful for testing or typing speech manually)
    if (showManualSpeechDialog) {
        QuickSpeechInputDialog(
            onDismiss = { showManualSpeechDialog = false },
            onTextSubmitted = { text, speaker ->
                viewModel.submitManualUtterance(text, speaker)
                showManualSpeechDialog = false
            }
        )
    }
}

// -----------------------------------------------------------------------------
// COMPOSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------

/**
 * Top App Bar matching Screenshot 2: "← Live" with History icon on the right
 */
@Composable
private fun LiveTopAppBar(
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                        onClick = onBackClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Live",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                    onClick = onHistoryClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Language Selector Row matching Screenshot 2: [ Japanese ▾ ] ⇄ [ English ▾ ]
 */
@Composable
private fun LiveLanguageSelectorRow(
    sourceLangName: String,
    targetLangName: String,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Source Language Pill
        LanguagePillButton(
            languageName = sourceLangName,
            onClick = onSourceClick,
            modifier = Modifier.testTag("source_language_pill")
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Swap Icon Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                    onClick = onSwapClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Target Language Pill
        LanguagePillButton(
            languageName = targetLangName,
            onClick = onTargetClick,
            modifier = Modifier.testTag("target_language_pill")
        )
    }
}

@Composable
private fun LanguagePillButton(
    languageName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialLivePillBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = languageName,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Select language",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Center Illustration for Idle state matching Screenshot 2:
 * Silhouette head speaking with sound waves speech bubble + "Tap the mic at the bottom to start"
 */
@Composable
private fun IdleCenterIllustration(
    onIllustrationClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onIllustrationClick
            )
    ) {
        // Custom Canvas drawing for the Head Silhouette & Speech Bubble with Audio Waves
        PersonSpeakingIllustration(
            modifier = Modifier.size(width = 160.dp, height = 140.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Tap the mic at the bottom to start",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Silhouette Graphic of a person speaking with a speech bubble with sound waves
 */
@Composable
private fun PersonSpeakingIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Silhouette color gradient
        val silhouetteBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF8BA3BC),
                Color(0xFF5A7B9D)
            ),
            startY = 0f,
            endY = h
        )

        // Draw Profile Head Silhouette (Left Side)
        val headPath = Path().apply {
            // Head curve
            moveTo(w * 0.28f, h * 0.78f)
            lineTo(w * 0.28f, h * 0.50f)
            cubicTo(
                w * 0.12f, h * 0.45f,
                w * 0.12f, h * 0.05f,
                w * 0.38f, h * 0.05f
            )
            cubicTo(
                w * 0.58f, h * 0.05f,
                w * 0.60f, h * 0.25f,
                w * 0.56f, h * 0.38f
            )
            // Nose & Lips Profile
            lineTo(w * 0.60f, h * 0.43f) // Nose tip
            lineTo(w * 0.53f, h * 0.47f)
            lineTo(w * 0.55f, h * 0.51f) // Upper lip
            lineTo(w * 0.52f, h * 0.53f) // Mouth
            lineTo(w * 0.54f, h * 0.56f) // Lower lip
            lineTo(w * 0.51f, h * 0.60f) // Chin
            cubicTo(
                w * 0.44f, h * 0.68f,
                w * 0.44f, h * 0.78f,
                w * 0.44f, h * 0.88f
            )
            lineTo(w * 0.28f, h * 0.88f)
            close()
        }
        drawPath(path = headPath, brush = silhouetteBrush)

        // Draw Speech Bubble on the right
        val bubbleCenter = Offset(w * 0.74f, h * 0.35f)
        val bubbleRadius = w * 0.18f

        // Bubble body
        drawCircle(
            brush = silhouetteBrush,
            radius = bubbleRadius,
            center = bubbleCenter
        )

        // Speech bubble tail
        val tailPath = Path().apply {
            moveTo(bubbleCenter.x - bubbleRadius * 0.8f, bubbleCenter.y + bubbleRadius * 0.3f)
            lineTo(bubbleCenter.x - bubbleRadius * 1.3f, bubbleCenter.y + bubbleRadius * 0.6f)
            lineTo(bubbleCenter.x - bubbleRadius * 0.3f, bubbleCenter.y + bubbleRadius * 0.9f)
            close()
        }
        drawPath(path = tailPath, brush = silhouetteBrush)

        // Sound wave arcs inside the speech bubble
        val waveColor = MaterialLiveBackgroundTop
        val strokeWidth = 3.5.dp.toPx()

        // Inner sound wave arc
        drawArc(
            color = waveColor,
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(bubbleCenter.x - bubbleRadius * 0.45f, bubbleCenter.y - bubbleRadius * 0.45f),
            size = Size(bubbleRadius * 0.9f, bubbleRadius * 0.9f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Outer sound wave arc
        drawArc(
            color = waveColor,
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(bubbleCenter.x - bubbleRadius * 0.15f, bubbleCenter.y - bubbleRadius * 0.75f),
            size = Size(bubbleRadius * 1.5f, bubbleRadius * 1.5f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Idle State Bottom Controls matching Screenshot 2:
 * [✓] Record audio
 * [Big Mic Button]
 * "Tap to start"
 */
@Composable
private fun IdleBottomControls(
    recordAudioEnabled: Boolean,
    onToggleRecordAudio: () -> Unit,
    onMicClick: () -> Unit,
    onQuickTestClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // [✓] Record audio Pill Checkbox
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialLivePillBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                    onClick = onToggleRecordAudio
                )
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (recordAudioEnabled) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = "Toggle record audio",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Record audio",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Large Center Microphone Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 42.dp, color = Color.White.copy(alpha = 0.35f)),
                        onClick = onMicClick
                    )
                    .testTag("live_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                // Large White/Ice-Blue Microphone Icon
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start recording",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Text(
                text = "Tap to start",
                color = MaterialLiveMutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * Centered prompt text matching Screenshot 1: "Listening... please speak"
 */
@Composable
private fun ListeningPromptText() {
    val infiniteTransition = rememberInfiniteTransition(label = "listening_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Text(
        text = "Listening... please speak",
        color = Color.White.copy(alpha = alpha),
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

/**
 * Live Transcripts Stream matching Screenshot 3:
 * Shows Japanese original text in soft silver and English translated text in bold white.
 */
@Composable
private fun LiveTranscriptsStream(
    segments: List<TranscriptSegment>,
    partialSource: String,
    partialTranslation: String,
    onSpeakSegment: (TranscriptSegment) -> Unit,
    onSpeakSource: (TranscriptSegment) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(segments.size, partialSource) {
        if (segments.isNotEmpty() || partialSource.isNotEmpty()) {
            listState.animateScrollToItem((segments.size + if (partialSource.isNotEmpty()) 1 else 0) - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Render completed historical segments with the latest highlighted
        itemsIndexed(segments) { index, segment ->
            val isLatest = index == segments.lastIndex && partialSource.isEmpty()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Original spoken text (e.g. "みようよ。")
                Text(
                    text = segment.sourceText,
                    color = if (isLatest) MaterialLiveLightText else MaterialLiveMutedText.copy(alpha = 0.8f),
                    fontSize = if (isLatest) 19.sp else 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Translated text (e.g. "Let's see.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = segment.translatedText,
                        color = if (isLatest) Color.White else Color.White.copy(alpha = 0.85f),
                        fontSize = if (isLatest) 28.sp else 22.sp,
                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.SemiBold,
                        lineHeight = 36.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Audio speak button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                                onClick = { onSpeakSegment(segment) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak translation",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Render current ongoing real-time partial speech if active
        if (partialSource.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = partialSource,
                        color = MaterialLiveLightText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 26.sp
                    )

                    if (partialTranslation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = partialTranslation,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom Controls for Active Recording state matching Screenshot 1 & 3:
 * ● 00:09 (Red dot + timer)
 * [Big Circular Pause Button (Outer white ring + Inner blue circle)]
 * "Tap to pause" / "Tap to resume"
 */
@Composable
private fun ActiveRecordingBottomControls(
    isListening: Boolean,
    elapsedSeconds: Int,
    onPauseResumeClick: () -> Unit,
    onSimulateSpeech: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Red Recording Dot + Timer Display (e.g. ● 00:09)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSimulateSpeech
            )
        ) {
            // Blinking red dot when listening
            val dotColor = if (isListening) {
                MaterialLiveRecordDot
            } else {
                MaterialLiveMutedText
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatTimerSeconds(elapsedSeconds),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        // Circular Pause / Resume Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(width = 2.5.dp, color = Color.White, shape = CircleShape)
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                        onClick = onPauseResumeClick
                    )
                    .testTag("live_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isListening) {
                    // Inner filled circular button (Screenshot 1 & 3)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialLivePauseInnerCircle)
                    )
                } else {
                    // When paused, show play/resume icon
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume recording",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = if (isListening) "Tap to pause" else "Tap to resume",
                color = MaterialLiveMutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * Format elapsed seconds into mm:ss (e.g. 00:09)
 */
private fun formatTimerSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
