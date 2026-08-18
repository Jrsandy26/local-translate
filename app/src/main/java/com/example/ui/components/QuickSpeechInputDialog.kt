package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderBottomLight
import com.example.ui.theme.GlassBorderTopLight
import com.example.ui.theme.GlassCardSurface
import com.example.ui.theme.GlassPillActiveBg
import com.example.ui.theme.GlassPillBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.theme.TextGlassSubtitle

@Composable
fun QuickSpeechInputDialog(
    onTextSubmitted: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var customText by remember { mutableStateOf("") }
    var selectedSpeaker by remember { mutableStateOf("Speaker 1") }

    val presetPhrases = listOf(
        "I would like to express my heartfelt gratitude for your presentations and your time today.",
        "When I saw your activity book, I had a good impression. I thought that you were pursuing your activities well and all that.",
        "The presentation here was quite informative and we are making great progress.",
        "Good morning everyone, thank you for joining us.",
        "Could you please explain this point in more detail?",
        "We are pleased to introduce our latest project results.",
        "Let's move on to the next topic on the agenda.",
        "Are there any questions or comments regarding this proposal?",
        "Thank you very much for your cooperation."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        modifier = Modifier.testTag("quick_speech_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Speech Stream",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGlassHeading
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextGlassSubtitle,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Speaker Selector Glass Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Speaker 1", "Speaker 2").forEach { spk ->
                        val isSelected = selectedSpeaker == spk
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = if (isSelected) 4.dp else 0.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    spotColor = NeonCyan.copy(alpha = 0.4f)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GlassPillActiveBg else GlassPillBg)
                                .border(
                                    width = 1.dp,
                                    brush = if (isSelected) {
                                        Brush.linearGradient(listOf(NeonCyan, Color(0xFF388BFF)))
                                    } else {
                                        Brush.linearGradient(listOf(GlassBorderTopLight, GlassBorderBottomLight))
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedSpeaker = spk }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonCyan else TextGlassSubtitle,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = spk,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonCyan else TextGlassHeading
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Input Field
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = { Text("Type custom speech...", color = TextGlassMuted, fontSize = 12.5.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_speech_text_field"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGlassHeading,
                        unfocusedTextColor = TextGlassBody,
                        focusedContainerColor = GlassCardSurface,
                        unfocusedContainerColor = GlassPillBg,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorderBottomLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SAMPLE SPEECH PHRASES",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Presets list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presetPhrases) { phrase ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlassPillBg)
                                .border(0.8.dp, GlassBorderBottomLight, RoundedCornerShape(10.dp))
                                .clickable {
                                    onTextSubmitted(phrase, selectedSpeaker)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = phrase,
                                fontSize = 12.sp,
                                color = TextGlassBody,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customText.isNotBlank()) {
                        onTextSubmitted(customText.trim(), selectedSpeaker)
                        onDismiss()
                    }
                },
                enabled = customText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = NeonCyan.copy(alpha = 0.4f))
                    .testTag("quick_speech_send_button")
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Speech", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGlassSubtitle, fontSize = 13.sp)
            }
        },
        shape = RoundedCornerShape(22.dp)
    )
}
