package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.model.Language
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.viewmodel.TranslationViewModel

@Composable
fun LanguagePacksScreen(viewModel: TranslationViewModel) {
    // Maintain local mock download state for the session
    val downloadedPacks = remember {
        mutableStateMapOf(
            "en" to true,
            "ja" to true,
            "es" to false,
            "zh" to false,
            "fr" to false,
            "de" to false
        )
    }

    val packSizes = remember {
        mapOf(
            "en" to "74 MB",
            "ja" to "118 MB",
            "es" to "68 MB",
            "zh" to "142 MB",
            "fr" to "82 MB",
            "de" to "89 MB"
        )
    }

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        size = 36.dp,
                        onClick = { viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Offline Language Packs",
                        color = TextGlassHeading,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informational Banner
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Downloaded packs enable neural on-device translations when you have no network access.",
                        color = TextGlassBody,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Languages list
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(Language.SUPPORTED_LANGUAGES) { language ->
                    val code = language.code
                    val isDownloaded = downloadedPacks[code] ?: false
                    val size = packSizes[code] ?: "80 MB"

                    LanguagePackRow(
                        language = language,
                        size = size,
                        isDownloaded = isDownloaded,
                        onDownloadClick = {
                            downloadedPacks[code] = !isDownloaded
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguagePackRow(
    language: Language,
    size: String,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isElevated = isDownloaded
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = language.name,
                    color = TextGlassHeading,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${language.nativeName}  •  $size",
                    color = TextGlassMuted,
                    fontSize = 12.sp
                )
            }

            if (isDownloaded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x1510B981))
                        .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable(onClick = onDownloadClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Installed",
                        color = NeonEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(20.dp))
                        .clickable(onClick = onDownloadClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Install",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
