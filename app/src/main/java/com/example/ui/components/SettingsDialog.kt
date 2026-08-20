package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppThemeMode
import com.example.ui.theme.AppTheme

@Composable
fun SettingsDialog(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    speechSpeed: Float,
    onSpeechSpeedChange: (Float) -> Unit,
    speechPitch: Float,
    onSpeechPitchChange: (Float) -> Unit,
    preferredVoiceGender: String,
    onVoiceGenderChange: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Preferences & Accessibility",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Theme Mode Switcher Section
                Text(
                    text = "App Theme",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "High-contrast dark mode reduces eye strain in long recording sessions",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3-Option Theme Switcher Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.pillBackground)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeOptionButton(
                        title = "Light",
                        icon = Icons.Default.LightMode,
                        isSelected = themeMode == AppThemeMode.LIGHT,
                        onClick = { onThemeModeChange(AppThemeMode.LIGHT) },
                        testTag = "theme_light_button",
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        title = "Dark",
                        icon = Icons.Default.DarkMode,
                        isSelected = themeMode == AppThemeMode.DARK,
                        onClick = { onThemeModeChange(AppThemeMode.DARK) },
                        testTag = "theme_dark_button",
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        title = "System",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = themeMode == AppThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(AppThemeMode.SYSTEM) },
                        testTag = "theme_system_button",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Voice Speech Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speech Speed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${String.format("%.2f", speechSpeed)}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                Slider(
                    value = speechSpeed,
                    onValueChange = onSpeechSpeedChange,
                    valueRange = 0.5f..1.5f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.pillBackground
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Speech Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speech Pitch",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${String.format("%.2f", speechPitch)}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                Slider(
                    value = speechPitch,
                    onValueChange = onSpeechPitchChange,
                    valueRange = 0.7f..1.3f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.pillBackground
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Preferred Voice Gender Selection
                Text(
                    text = "Preferred Voice Tone",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val voiceOptions = listOf("Default", "Female", "Male")
                    voiceOptions.forEach { option ->
                        val isSelected = option == preferredVoiceGender
                        Button(
                            onClick = { onVoiceGenderChange(option) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.primary else colors.pillBackground,
                                contentColor = if (isSelected) Color.White else colors.textSecondary
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

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
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Translation History")
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val bgColor = if (isSelected) colors.surface else Color.Transparent
    val contentColor = if (isSelected) colors.primary else colors.textSecondary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
