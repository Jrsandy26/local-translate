package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.theme.TextGlassSubtitle
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun TranslateHomeScreen(viewModel: TranslationViewModel) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val homeInputText by viewModel.homeInputText.collectAsState()
    val homeTranslatedText by viewModel.homeTranslatedText.collectAsState()
    val showLanguageSheet by viewModel.showLanguageSheet.collectAsState()
    val isSelectingSourceLanguage by viewModel.isSelectingSourceLanguage.collectAsState()

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / App Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "App Logo",
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LinguaGlass",
                        color = TextGlassHeading,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Row of top buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = Icons.Default.History,
                        contentDescription = "Translation History",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.HISTORY) }
                    )
                    GlassIconButton(
                        icon = Icons.Default.Language,
                        contentDescription = "Language Packs",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.LANGUAGE_PACKS) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Language Selector Component
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source Language Button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.isSelectingSourceLanguage.value = true
                                viewModel.showLanguageSheet.value = true
                            }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("FROM", color = TextGlassMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sourceLang.name, color = TextGlassHeading, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Swap Button
                    GlassIconButton(
                        icon = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Languages",
                        size = 36.dp,
                        onClick = { viewModel.swapLanguages() }
                    )

                    // Target Language Button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.isSelectingSourceLanguage.value = false
                                viewModel.showLanguageSheet.value = true
                            }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TO", color = TextGlassMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(targetLang.name, color = TextGlassHeading, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Input Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isElevated = true
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sourceLang.name,
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (homeInputText.isNotEmpty()) {
                            IconButtonWithRipple(
                                icon = Icons.Default.Clear,
                                contentDescription = "Clear input",
                                onClick = { viewModel.clearHomeInput() }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = homeInputText,
                        onValueChange = { viewModel.setHomeInputText(it) },
                        placeholder = { Text("Enter text to translate...", color = TextGlassMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("text_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextGlassHeading,
                            unfocusedTextColor = TextGlassBody
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, lineHeight = 24.sp)
                    )

                    if (homeInputText.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            GlassIconButton(
                                icon = Icons.Default.VolumeUp,
                                contentDescription = "Speak source",
                                size = 32.dp,
                                onClick = { viewModel.speakHomeSource() }
                            )
                        }
                    }
                }
            }

            // Translation Card (only shown when translated text is available)
            if (homeTranslatedText.isNotEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isElevated = false,
                    isActive = true
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = targetLang.name,
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            GlassIconButton(
                                icon = Icons.Default.VolumeUp,
                                contentDescription = "Speak translation",
                                size = 32.dp,
                                onClick = { viewModel.speakHomeTranslation() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = homeTranslatedText,
                            color = TextGlassHeading,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Actions: Face to Face, Live Session Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    text = "Face to Face",
                    onClick = { viewModel.setActiveScreen(ActiveScreen.FACE_TO_FACE) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.SwapHoriz,
                    gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                )

                GlassButton(
                    text = "Live",
                    onClick = { viewModel.startNewLiveSession() },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Mic,
                    gradientColors = listOf(NeonCyan, Color(0xFF0066FF))
                )
            }
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
}

@Composable
fun IconButtonWithRipple(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextGlassMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
