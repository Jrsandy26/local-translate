package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.ui.components.LanguagePillSelector
import com.example.ui.theme.*
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun LiveTranslateScreen(
    viewModel: TranslationViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val segments by viewModel.liveSegments.collectAsState()
    val partialText by viewModel.livePartialText.collectAsState()
    val partialTranslated by viewModel.livePartialTranslated.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Live Translate",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Language Pill
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LanguagePillSelector(
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transcript conversation list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (segments.isEmpty() && partialText.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap the microphone to start real-time translation",
                            fontSize = 15.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            items(segments) { seg ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = seg.speaker,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = seg.sourceText,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFF1F1F8)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = seg.translatedText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PurpleDark,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.speakText(seg.translatedText, targetLang.code) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real-time live streaming segment
            if (partialText.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF8FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Listening...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentLive
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = partialText, fontSize = 15.sp, color = TextPrimary)
                            if (partialTranslated.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = partialTranslated,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PurplePrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Audio Visualizer & Mic Control Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated audio wave bars
            if (isListening) {
                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(8) { index ->
                        val barHeight = (12 + (rmsLevel * 20 * (index % 3 + 1))).coerceIn(8f, 28f)
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(PurplePrimary)
                        )
                    }
                }
            }

            // Big Action Mic Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PurpleGradientStart, PurpleGradientEnd)
                        )
                    )
                    .clickable { viewModel.toggleMicListening() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
