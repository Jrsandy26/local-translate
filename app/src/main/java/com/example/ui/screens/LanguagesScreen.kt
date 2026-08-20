package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.translation.GoogleTranslationEngine
import com.example.ui.theme.AppBackground
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch

@Composable
fun LanguagesScreen(
    viewModel: TranslationViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var downloadedCodes by remember { mutableStateOf(setOf("en")) }

    LaunchedEffect(Unit) {
        Language.ALL_LANGUAGES.forEach { lang ->
            if (GoogleTranslationEngine.isModelDownloaded(lang.code)) {
                downloadedCodes = downloadedCodes + lang.code
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Title
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
            items(Language.ALL_LANGUAGES) { lang ->
                val isDownloaded = downloadedCodes.contains(lang.code)

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

                        if (isDownloaded) {
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
                        } else {
                            Button(
                                onClick = {
                                    GoogleTranslationEngine.downloadModel(
                                        langCode = lang.code,
                                        onSuccess = {
                                            downloadedCodes = downloadedCodes + lang.code
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleLight, contentColor = PurplePrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
