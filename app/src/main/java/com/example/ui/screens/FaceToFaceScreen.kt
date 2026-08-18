package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
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

    val speaker1Text by viewModel.faceToFaceSpeaker1Text.collectAsState()
    val speaker1Trans by viewModel.faceToFaceSpeaker1Trans.collectAsState()

    val speaker2Text by viewModel.faceToFaceSpeaker2Text.collectAsState()
    val speaker2Trans by viewModel.faceToFaceSpeaker2Trans.collectAsState()

    var showSpeechDialogSpeaker by remember { mutableStateOf<Int?>(null) }

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Flipped Top Half: Guest / Speaker 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer { rotationZ = 180f } // Rotated 180 degrees
                    .padding(16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    isActive = speaker2Text.isNotEmpty()
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
                                text = targetLang.name,
                                color = Color(0xFFC084FC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "GUEST PANE",
                                color = TextGlassMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f).verticalScrollable()) {
                            if (speaker2Text.isEmpty()) {
                                Text(
                                    text = "Tap Speak to translate to guest...",
                                    color = TextGlassMuted,
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = speaker2Text,
                                    color = TextGlassHeading,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = speaker2Trans,
                                    color = NeonEmerald,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MicIconButton(
                                onClick = { showSpeechDialogSpeaker = 2 },
                                modifier = Modifier.testTag("speaker2_mic_button")
                            )
                        }
                    }
                }
            }

            // Divider Line with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0x20FFFFFF))
                )
                Spacer(modifier = Modifier.width(10.dp))
                GlassIconButton(
                    icon = Icons.Default.ArrowBack,
                    contentDescription = "Exit Face-to-Face",
                    size = 40.dp,
                    onClick = { viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME) }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0x20FFFFFF))
                )
            }

            // Bottom Half: Host / Speaker 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    isActive = speaker1Text.isNotEmpty()
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
                                text = sourceLang.name,
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "HOST PANE",
                                color = TextGlassMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f).verticalScrollable()) {
                            if (speaker1Text.isEmpty()) {
                                Text(
                                    text = "Tap Speak to translate to host...",
                                    color = TextGlassMuted,
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = speaker1Text,
                                    color = TextGlassHeading,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = speaker1Trans,
                                    color = NeonEmerald,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MicIconButton(
                                onClick = { showSpeechDialogSpeaker = 1 },
                                modifier = Modifier.testTag("speaker1_mic_button")
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Speech Input Dialog
    showSpeechDialogSpeaker?.let { speaker ->
        QuickSpeechInputDialog(
            onDismiss = { showSpeechDialogSpeaker = null },
            onTextSubmitted = { text, speakerName ->
                viewModel.processFaceToFaceSpeech(speaker, text)
                showSpeechDialogSpeaker = null
            }
        )
    }
}

@Composable
fun MicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .border(1.5.dp, Brush.linearGradient(listOf(NeonCyan, Color(0xFF0055FF))), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Speak",
            tint = NeonCyan,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun Modifier.verticalScrollable() = this.then(
    Modifier.graphicsLayer { clip = true }
)
