package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun ConversationScreen(
    viewModel: TranslationViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val activeSpeaker by viewModel.activeConversationSpeaker.collectAsState()
    val speaker1Text by viewModel.speaker1Text.collectAsState()
    val speaker1Translated by viewModel.speaker1Translated.collectAsState()
    val speaker2Text by viewModel.speaker2Text.collectAsState()
    val speaker2Translated by viewModel.speaker2Translated.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                text = "Two-Way Conversation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Top Half (Partner - Inverted 180 deg for Face-to-Face)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AccentConversationBg)
                .rotate(180f)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${targetLang.flag} ${targetLang.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentConversation
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = speaker2Text.ifEmpty { "Tap mic below to speak ${targetLang.name}" },
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (speaker2Translated.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = speaker2Translated,
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Center Action Split Controller
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speaker 2 Mic
            Button(
                onClick = {
                    viewModel.activeConversationSpeaker.value = 2
                    viewModel.toggleMicListening()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening && activeSpeaker == 2) Color(0xFFE53935) else AccentConversation
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("${targetLang.code.uppercase()} Mic")
            }

            // Speaker 1 Mic
            Button(
                onClick = {
                    viewModel.activeConversationSpeaker.value = 1
                    viewModel.toggleMicListening()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening && activeSpeaker == 1) Color(0xFFE53935) else PurplePrimary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("${sourceLang.code.uppercase()} Mic")
            }
        }

        // Bottom Half (User)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PurpleLight)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${sourceLang.flag} ${sourceLang.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = speaker1Text.ifEmpty { "Tap mic above to speak ${sourceLang.name}" },
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (speaker1Translated.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = speaker1Translated,
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
