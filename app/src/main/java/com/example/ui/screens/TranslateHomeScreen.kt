package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.QuickSpeechInputDialog
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun TranslateHomeScreen(viewModel: TranslationViewModel) {
    val context = LocalContext.current
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val homeInputText by viewModel.homeInputText.collectAsState()
    val homeTranslatedText by viewModel.homeTranslatedText.collectAsState()
    val showLanguageSheet by viewModel.showLanguageSheet.collectAsState()
    val isSelectingSourceLanguage by viewModel.isSelectingSourceLanguage.collectAsState()

    var showDictationDialog by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 400),
        label = "rotation"
    )

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Riva Translate",
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Riva Translate",
                        color = TextGlassHeading,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = Icons.Default.School,
                        contentDescription = "Language Learning",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.LANGUAGE_LEARNING) }
                    )
                    GlassIconButton(
                        icon = Icons.Default.History,
                        contentDescription = "History",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.HISTORY) }
                    )
                    GlassIconButton(
                        icon = Icons.Default.Language,
                        contentDescription = "Language Packs",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.LANGUAGE_PACKS) }
                    )
                    GlassIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.SETTINGS) }
                    )
                }
            }

            // Language Selector Panel
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source Language Selector
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.isSelectingSourceLanguage.value = true
                                viewModel.showLanguageSheet.value = true
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("FROM", color = TextGlassMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sourceLang.name, color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // Swap Button
                    GlassIconButton(
                        icon = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Languages",
                        size = 38.dp,
                        modifier = Modifier.rotate(animatedRotation),
                        onClick = {
                            rotationAngle += 180f
                            viewModel.swapLanguages()
                        }
                    )

                    // Target Language Selector
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.isSelectingSourceLanguage.value = false
                                viewModel.showLanguageSheet.value = true
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TO", color = TextGlassMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(targetLang.name, color = Color(0xFFC084FC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Large Text Input Card with Dictation Mic
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
                            text = "${sourceLang.name} (Source Text)",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (homeInputText.isNotEmpty()) {
                                GlassIconButton(
                                    icon = Icons.Default.VolumeUp,
                                    contentDescription = "Speak source",
                                    size = 30.dp,
                                    onClick = { viewModel.speakHomeSource() }
                                )
                                GlassIconButton(
                                    icon = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    size = 30.dp,
                                    onClick = { viewModel.clearHomeInput() }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        OutlinedTextField(
                            value = homeInputText,
                            onValueChange = { viewModel.setHomeInputText(it) },
                            placeholder = { Text("Type, paste, or speak text to translate...", color = TextGlassMuted) },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("text_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextGlassHeading,
                                unfocusedTextColor = TextGlassBody
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 17.sp, lineHeight = 24.sp)
                        )
                    }

                    // Bottom Action Row inside Input Card (Mic Dictation & Paste)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dictation Mic Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .clickable { showDictationDialog = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Dictation",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dictate", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Paste Button
                        GlassIconButton(
                            icon = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            size = 30.dp,
                            onClick = {
                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = clipboard.primaryClip
                                    if (clipData != null && clipData.itemCount > 0) {
                                        val pasteText = clipData.getItemAt(0).text.toString()
                                        if (pasteText.isNotBlank()) {
                                            viewModel.setHomeInputText(pasteText)
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            // Real-time Translation Output Card
            AnimatedVisibility(visible = homeTranslatedText.isNotEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isActive = true
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${targetLang.name} (Translation)",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Translation Actions: Copy, Speak, Star / Save
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GlassIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Translation",
                                    size = 32.dp,
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Translated Text", homeTranslatedText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                GlassIconButton(
                                    icon = Icons.Default.VolumeUp,
                                    contentDescription = "Speak Translation",
                                    size = 32.dp,
                                    onClick = { viewModel.speakHomeTranslation() }
                                )

                                GlassIconButton(
                                    icon = Icons.Default.Star,
                                    contentDescription = "Save Translation",
                                    size = 32.dp,
                                    onClick = { viewModel.saveHomeTranslationToHistory() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = homeTranslatedText,
                            color = TextGlassHeading,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Central Translation Services Quick Launcher
            Text(
                text = "TRANSLATION SERVICES",
                color = TextGlassMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Live Translation & Conversation Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Live Session",
                        onClick = { viewModel.startNewLiveSession() },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Mic,
                        gradientColors = listOf(NeonCyan, Color(0xFF0066FF))
                    )

                    GlassButton(
                        text = "Conversation",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.FACE_TO_FACE) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.SwapHoriz,
                        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                    )
                }

                // Row 2: Language Learning & Offline Model Packs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "Learn & Practice",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.LANGUAGE_LEARNING) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.School,
                        gradientColors = listOf(NeonEmerald, Color(0xFF059669))
                    )

                    GlassButton(
                        text = "Offline Packs",
                        onClick = { viewModel.setActiveScreen(ActiveScreen.LANGUAGE_PACKS) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Language,
                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                    )
                }
            }
        }
    }

    // Dictation speech input dialog
    if (showDictationDialog) {
        QuickSpeechInputDialog(
            onDismiss = { showDictationDialog = false },
            onTextSubmitted = { text, _ ->
                viewModel.setHomeInputText(text)
                showDictationDialog = false
            }
        )
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
