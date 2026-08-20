package com.rivatranslate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivatranslate.model.ActiveScreen
import com.rivatranslate.model.AppThemeMode
import com.rivatranslate.ui.components.GlowingMicButton
import com.rivatranslate.ui.components.LanguagePillSelector
import com.rivatranslate.ui.components.QuickActionCard
import com.rivatranslate.ui.components.RecentTranslationCard
import com.rivatranslate.ui.theme.AppTheme
import com.rivatranslate.ui.viewmodel.TranslationViewModel

@Composable
fun TranslateHomeScreen(
    viewModel: TranslationViewModel,
    onNavigateTo: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val inputText by viewModel.homeInputText.collectAsState()
    val translatedText by viewModel.homeTranslatedText.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val recentTranslations by viewModel.recentTranslations.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // 1. Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title "Translate" with "Trans" and "late"
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("Trans")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("late")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Communication across languages",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }

                // Top Right Action Buttons (Theme Switcher, Notifications, Settings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme Quick Toggle Button (Light/Dark mode)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.pillBackground)
                            .clickable { viewModel.toggleThemeMode() }
                            .testTag("theme_quick_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (colors.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (colors.isDark) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Notification Button with Indicator Dot
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.pillBackground)
                            .clickable { viewModel.showNotificationsDialog.value = true }
                            .testTag("notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 12.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.pillBackground)
                            .clickable { viewModel.showSettingsDialog.value = true }
                            .testTag("settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Main Translation Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 2.dp else 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = colors.primary.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Badge: Sparkle + "Enter text to translate"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Enter text to translate",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Editable Input Area with Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type, paste or speak to translate...",
                                fontSize = 16.sp,
                                color = colors.textMuted,
                                lineHeight = 22.sp
                            )
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = { viewModel.onHomeInputChanged(it) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = colors.textPrimary,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(colors.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Translated Result Live Preview (if typed)
                    if (translatedText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.pillBackground)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = targetLang.name.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = translatedText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.speakText(translatedText, targetLang.code) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Row: Language Pill Selector + Glowing Mic Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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

                        GlowingMicButton(
                            isListening = isListening,
                            onClick = { viewModel.toggleMicListening() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Quick Actions Section
        item {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Grid Layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Live Translate & Conversation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Live Translate",
                        subtitle = "Real-time\nconversation",
                        icon = Icons.Default.GraphicEq,
                        iconColor = colors.accentLive,
                        iconBgColor = colors.accentLiveBg,
                        onClick = { onNavigateTo(ActiveScreen.LIVE_TRANSLATE) },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Conversation",
                        subtitle = "Two-way\ntranslation",
                        icon = Icons.Default.ChatBubbleOutline,
                        iconColor = colors.accentConversation,
                        iconBgColor = colors.accentConversationBg,
                        onClick = { onNavigateTo(ActiveScreen.CONVERSATION) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: History & Saved
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "History",
                        subtitle = "View your past\ntranslations",
                        icon = Icons.Default.History,
                        iconColor = colors.accentHistory,
                        iconBgColor = colors.accentHistoryBg,
                        onClick = { onNavigateTo(ActiveScreen.HISTORY) },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Saved",
                        subtitle = "Your saved\ntranslations",
                        icon = Icons.Default.Star,
                        iconColor = colors.accentSaved,
                        iconBgColor = colors.accentSavedBg,
                        onClick = { onNavigateTo(ActiveScreen.SAVED) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Recent Translations Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Translations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = "See all",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                    modifier = Modifier
                        .clickable { onNavigateTo(ActiveScreen.HISTORY) }
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Recent items list
        if (recentTranslations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent translations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your offline translation history will show up here.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentTranslations.take(5), key = { it.id }) { item ->
                RecentTranslationCard(
                    item = item,
                    onToggleFavorite = { viewModel.toggleFavorite(item) },
                    onSpeak = { viewModel.speakText(item.translatedText, item.targetLangCode) },
                    onDelete = { viewModel.deleteRecentTranslation(item) },
                    onClick = {
                        viewModel.homeInputText.value = item.sourceText
                        viewModel.onHomeInputChanged(item.sourceText)
                    },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}
