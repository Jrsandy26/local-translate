package com.example.ui.screens

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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.model.RecentTranslation
import com.example.ui.components.GlowingMicButton
import com.example.ui.components.LanguagePillSelector
import com.example.ui.components.QuickActionCard
import com.example.ui.components.RecentTranslationCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun TranslateHomeScreen(
    viewModel: TranslationViewModel,
    onNavigateTo: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val inputText by viewModel.homeInputText.collectAsState()
    val translatedText by viewModel.homeTranslatedText.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val recentTranslations by viewModel.recentTranslations.collectAsState()



    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
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
                // Title "Translate" with "Trans" (black) and "late" (purple)
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("Trans")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = PurplePrimary,
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
                        color = TextSecondary
                    )
                }

                // Top Right Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification Button with Unread Dot
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F1F8))
                            .clickable { viewModel.showNotificationsDialog.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        // Purple Notification Dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 12.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F1F8))
                            .clickable { viewModel.showSettingsDialog.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary,
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
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0x0D6C5CE7)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                .background(PurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Enter text to translate",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurplePrimary
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
                                color = TextMuted,
                                lineHeight = 22.sp
                            )
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = { viewModel.onHomeInputChanged(it) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(PurplePrimary),
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
                                .background(Color(0xFFF7F8FC))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = targetLang.name.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = translatedText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.speakText(translatedText, targetLang.code) },
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
                color = TextPrimary
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
                        iconColor = AccentLive,
                        iconBgColor = AccentLiveBg,
                        onClick = { onNavigateTo(ActiveScreen.LIVE_TRANSLATE) },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Conversation",
                        subtitle = "Two-way\ntranslation",
                        icon = Icons.Default.ChatBubbleOutline,
                        iconColor = AccentConversation,
                        iconBgColor = AccentConversationBg,
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
                        iconColor = AccentHistory,
                        iconBgColor = AccentHistoryBg,
                        onClick = { onNavigateTo(ActiveScreen.HISTORY) },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionCard(
                        title = "Saved",
                        subtitle = "Your saved\ntranslations",
                        icon = Icons.Default.Star,
                        iconColor = AccentSaved,
                        iconBgColor = AccentSavedBg,
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
                    color = TextPrimary
                )

                Text(
                    text = "See all",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
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
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                            tint = PurplePrimary.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent translations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your offline translation history will show up here.",
                            fontSize = 13.sp,
                            color = TextSecondary,
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
