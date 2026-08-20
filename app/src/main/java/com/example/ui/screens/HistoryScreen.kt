package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.ui.components.RecentTranslationCard
import com.example.ui.theme.AppBackground
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun HistoryScreen(
    viewModel: TranslationViewModel,
    onlyFavorites: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentTranslations by viewModel.recentTranslations.collectAsState()
    val favoriteTranslations by viewModel.favoriteTranslations.collectAsState()

    val displayList = if (onlyFavorites) favoriteTranslations else recentTranslations

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = if (onlyFavorites) "Saved Translations" else "Translation History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (!onlyFavorites && displayList.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color(0xFFE53935))
                }
            }
        }

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (onlyFavorites) "No saved favorites yet" else "No history yet",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayList, key = { it.id }) { item ->
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
