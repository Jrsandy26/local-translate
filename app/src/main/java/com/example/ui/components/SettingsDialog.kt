package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    speechSpeed: Float,
    onSpeechSpeedChange: (Float) -> Unit,
    speechPitch: Float,
    onSpeechPitchChange: (Float) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice & App Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Speech Speed Slider
                Text(
                    text = "Speech Speed: ${String.format("%.2f", speechSpeed)}x",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Slider(
                    value = speechSpeed,
                    onValueChange = onSpeechSpeedChange,
                    valueRange = 0.5f..1.5f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = PurplePrimary,
                        activeTrackColor = PurplePrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pitch Slider
                Text(
                    text = "Speech Pitch: ${String.format("%.2f", speechPitch)}x",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Slider(
                    value = speechPitch,
                    onValueChange = onSpeechPitchChange,
                    valueRange = 0.7f..1.3f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = PurplePrimary,
                        activeTrackColor = PurplePrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Clear History Button
                OutlinedButton(
                    onClick = {
                        onClearHistory()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Translation History")
                }
            }
        }
    }
}
