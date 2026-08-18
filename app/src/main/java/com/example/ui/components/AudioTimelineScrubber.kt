package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkPillActive
import com.example.ui.theme.DarkPillContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AudioTimelineScrubber(
    currentPositionSeconds: Int,
    totalDurationSeconds: Int,
    isPlaying: Boolean,
    isLiveRecording: Boolean,
    onSeek: (Int) -> Unit,
    onPlayPauseClick: () -> Unit,
    onRecordToggleClick: () -> Unit,
    onSimulateSpeechClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFormatted = formatTime(currentPositionSeconds)
    val totalFormatted = formatTime(totalDurationSeconds.coerceAtLeast(currentPositionSeconds))

    val maxVal = if (totalDurationSeconds > 0) totalDurationSeconds.toFloat() else 1f
    val currentVal = currentPositionSeconds.toFloat().coerceIn(0f, maxVal)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(
                width = 1.dp,
                color = DarkCardBorder,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Scrubber slider row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentFormatted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark,
                modifier = Modifier.testTag("current_time_text")
            )

            Slider(
                value = currentVal,
                onValueChange = { newValue ->
                    onSeek(newValue.toInt())
                },
                valueRange = 0f..maxVal,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimaryDark,
                    activeTrackColor = AccentBlue,
                    inactiveTrackColor = DarkSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .height(26.dp)
                    .testTag("audio_seek_slider")
            )

            Text(
                text = totalFormatted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark,
                modifier = Modifier.testTag("total_time_text")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Quick speech simulation button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkPillContainer)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                    .clickable { onSimulateSpeechClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("bottom_simulate_speech_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.RecordVoiceOver,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "+ Test Speech",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )
            }

            // Central / Right Action Buttons: Playback & Live Recording Mic
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play / Pause Audio Transcript
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkPillContainer)
                        .border(1.dp, DarkCardBorder, CircleShape)
                        .clickable { onPlayPauseClick() }
                        .testTag("playback_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Playback" else "Play Transcript Audio",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Main Live Record Mic Button (Pulsing Red when recording, Blue/Red when idle)
                val buttonColor = if (isLiveRecording) AccentRed else AccentBlue
                val buttonScale by animateFloatAsState(
                    targetValue = if (isLiveRecording) 1.08f else 1.0f,
                    label = "action_btn_scale"
                )

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .scale(buttonScale)
                        .background(buttonColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onRecordToggleClick() }
                        .padding(horizontal = 14.dp)
                        .testTag("main_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedContent(
                            targetState = isLiveRecording,
                            label = "action_icon_transition"
                        ) { recording ->
                            if (recording) {
                                Icon(
                                    imageVector = Icons.Filled.Stop,
                                    contentDescription = "Stop Live Recording",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Start Live Mic",
                                    tint = Color(0xFF0F1A2C),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isLiveRecording) "Stop Mic" else "Live Mic",
                            color = if (isLiveRecording) Color.White else Color(0xFF0F1A2C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(totalSec: Int): String {
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d", minutes, seconds)
}


