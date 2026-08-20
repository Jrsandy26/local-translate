package com.rivatranslate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
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
import com.rivatranslate.model.Language
import com.rivatranslate.translation.GoogleTranslationEngine
import com.rivatranslate.ui.theme.AppBackground
import com.rivatranslate.ui.theme.PurpleLight
import com.rivatranslate.ui.theme.PurplePrimary
import com.rivatranslate.ui.theme.TextPrimary
import com.rivatranslate.ui.theme.TextSecondary
import com.rivatranslate.ui.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch

@Composable
fun LanguagesScreen(
    viewModel: TranslationViewModel,
    modifier: Modifier = Modifier
) {
    val downloadedCodes by viewModel.downloadedCodes.collectAsState()
    val downloadingCodes by viewModel.downloadingCodes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshDownloadedModels()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // ... Title and Description ...
        Text(
            text = "Language Packs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Download on-device offline translation neural models (~30MB each)",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = Language.ALL_LANGUAGES,
                key = { it.code } // Add key for better performance
            ) { lang ->
                val isDownloaded = downloadedCodes.contains(lang.code)
                val isDownloading = downloadingCodes.contains(lang.code)

                LanguageItem(
                    lang = lang,
                    isDownloaded = isDownloaded,
                    isDownloading = isDownloading,
                    onDownload = { viewModel.downloadLanguageModel(lang.code) },
                    onDelete = { viewModel.deleteLanguageModel(lang.code) }
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    lang: Language,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = lang.flag, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = lang.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = lang.nativeName,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(contentAlignment = Alignment.Center) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = PurplePrimary
                    )
                } else if (isDownloaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ready",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }

                        if (lang.code != "en") {
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Model",
                                    tint = Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleLight,
                            contentColor = PurplePrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
