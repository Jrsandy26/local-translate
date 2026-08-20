package com.rivatranslate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivatranslate.model.Language
import com.rivatranslate.model.RecentTranslation
import com.rivatranslate.model.TranscriptSegment
import com.rivatranslate.model.TranslationSession
import com.rivatranslate.ui.components.BulkExportDialog
import com.rivatranslate.ui.components.ExportSessionDialog
import com.rivatranslate.ui.components.RecentTranslationCard
import com.rivatranslate.ui.components.SessionPlaybackDialog
import com.rivatranslate.ui.theme.AppTheme
import com.rivatranslate.ui.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryTab {
    SESSIONS,
    TRANSLATIONS,
    FAVORITES
}

@Composable
fun HistoryScreen(
    viewModel: TranslationViewModel,
    onlyFavorites: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val recentTranslations by viewModel.recentTranslations.collectAsState()
    val favoriteTranslations by viewModel.favoriteTranslations.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    val showDetailDialog by viewModel.showSessionDetailDialog.collectAsState()
    val activeDetailSession by viewModel.activeDetailSession.collectAsState()
    val activeDetailSegments by viewModel.activeDetailSegments.collectAsState()

    var selectedTab by remember(onlyFavorites) {
        mutableStateOf(if (onlyFavorites) HistoryTab.FAVORITES else HistoryTab.SESSIONS)
    }

    var exportSessionTarget by remember { mutableStateOf<TranslationSession?>(null) }
    var exportSessionSegments by remember { mutableStateOf<List<TranscriptSegment>>(emptyList()) }
    var showBulkExportDialog by remember { mutableStateOf(false) }

    if (exportSessionTarget != null) {
        ExportSessionDialog(
            session = exportSessionTarget!!,
            segments = exportSessionSegments,
            onDismiss = {
                exportSessionTarget = null
                exportSessionSegments = emptyList()
            }
        )
    }

    if (showBulkExportDialog) {
        val listToExport = if (selectedTab == HistoryTab.FAVORITES || onlyFavorites) favoriteTranslations else recentTranslations
        BulkExportDialog(
            translations = listToExport,
            title = if (selectedTab == HistoryTab.FAVORITES || onlyFavorites) "Export Saved Favorites" else "Export All Translations",
            onDismiss = { showBulkExportDialog = false }
        )
    }

    if (showDetailDialog && activeDetailSession != null) {
        SessionPlaybackDialog(
            session = activeDetailSession,
            segments = activeDetailSegments,
            viewModel = viewModel,
            onDismiss = {
                viewModel.showSessionDetailDialog.value = false
            },
            onDeleteSession = {
                activeDetailSession?.let { viewModel.deleteSession(it) }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .testTag("history_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (onlyFavorites) "Saved Translations" else "Translation History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val hasExportableItems = when {
                    onlyFavorites || selectedTab == HistoryTab.FAVORITES -> favoriteTranslations.isNotEmpty()
                    selectedTab == HistoryTab.TRANSLATIONS -> recentTranslations.isNotEmpty()
                    selectedTab == HistoryTab.SESSIONS -> sessions.isNotEmpty()
                    else -> false
                }

                if (hasExportableItems) {
                    IconButton(
                        onClick = {
                            if (selectedTab == HistoryTab.SESSIONS && sessions.isNotEmpty()) {
                                coroutineScope.launch {
                                    val firstSession = sessions.first()
                                    val segs = viewModel.getSessionSegments(firstSession.id)
                                    exportSessionTarget = firstSession
                                    exportSessionSegments = segs
                                }
                            } else {
                                showBulkExportDialog = true
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .testTag("history_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export History",
                            tint = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (!onlyFavorites) {
                    IconButton(
                        onClick = {
                            when (selectedTab) {
                                HistoryTab.SESSIONS -> viewModel.clearAllSessions()
                                HistoryTab.TRANSLATIONS -> viewModel.clearAllHistory()
                                HistoryTab.FAVORITES -> {}
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color(0xFFE53935))
                    }
                }
            }
        }

        // Tab Selector Row
        if (!onlyFavorites) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabPill(
                    title = "Live Sessions (${sessions.size})",
                    isSelected = selectedTab == HistoryTab.SESSIONS,
                    onClick = { selectedTab = HistoryTab.SESSIONS },
                    modifier = Modifier.weight(1f)
                )

                TabPill(
                    title = "Translations (${recentTranslations.size})",
                    isSelected = selectedTab == HistoryTab.TRANSLATIONS,
                    onClick = { selectedTab = HistoryTab.TRANSLATIONS },
                    modifier = Modifier.weight(1f)
                )

                TabPill(
                    title = "Favorites (${favoriteTranslations.size})",
                    isSelected = selectedTab == HistoryTab.FAVORITES,
                    onClick = { selectedTab = HistoryTab.FAVORITES },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Content
        when (selectedTab) {
            HistoryTab.SESSIONS -> {
                if (sessions.isEmpty()) {
                    EmptyHistoryPlaceholder(
                        title = "No Live Sessions yet",
                        subtitle = "Recorded live translation sessions will appear here with audio playback and subtitle export."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            LiveSessionHistoryCard(
                                session = session,
                                onClick = { viewModel.openSessionDetail(session) },
                                onExport = {
                                    coroutineScope.launch {
                                        val segs = viewModel.getSessionSegments(session.id)
                                        exportSessionTarget = session
                                        exportSessionSegments = segs
                                    }
                                },
                                onDelete = { viewModel.deleteSession(session) }
                            )
                        }
                    }
                }
            }
            HistoryTab.TRANSLATIONS -> {
                if (recentTranslations.isEmpty()) {
                    EmptyHistoryPlaceholder(
                        title = "No translations yet",
                        subtitle = "Translated phrases from Home will appear here."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recentTranslations, key = { it.id }) { item ->
                            RecentTranslationCard(
                                item = item,
                                onToggleFavorite = { viewModel.toggleFavorite(item) },
                                onSpeak = { viewModel.speakText(item.translatedText, item.targetLangCode) },
                                onDelete = { viewModel.deleteRecentTranslation(item) },
                                onClick = {}
                            )
                        }
                    }
                }
            }
            HistoryTab.FAVORITES -> {
                if (favoriteTranslations.isEmpty()) {
                    EmptyHistoryPlaceholder(
                        title = "No saved favorites",
                        subtitle = "Tap the star icon on any translation to save it here."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(favoriteTranslations, key = { it.id }) { item ->
                            RecentTranslationCard(
                                item = item,
                                onToggleFavorite = { viewModel.toggleFavorite(item) },
                                onSpeak = { viewModel.speakText(item.translatedText, item.targetLangCode) },
                                onDelete = { viewModel.deleteRecentTranslation(item) },
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else colors.textSecondary
        )
    }
}

@Composable
private fun LiveSessionHistoryCard(
    session: TranslationSession,
    onClick: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    val srcLang = Language.findByCode(session.sourceLanguageCode)
    val tgtLang = Language.findByCode(session.targetLanguageCode)
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(session.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Round Play icon container
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colors.primaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Session",
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${srcLang.flag} ${srcLang.name} ➔ ${tgtLang.flag} ${tgtLang.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateStr,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.pillBackground)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${session.durationSeconds}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExport,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Subtitles & PDF",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder(title: String, subtitle: String) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = colors.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
