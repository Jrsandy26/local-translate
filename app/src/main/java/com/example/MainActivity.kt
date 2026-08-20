package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.model.ActiveScreen
import com.example.ui.components.BottomNavBar
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.*
import com.example.ui.theme.RivaTranslateTheme
import com.example.ui.viewmodel.TranslationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RivaTranslateTheme {
                val activeScreen by viewModel.activeScreen.collectAsState()
                val showLangSelector by viewModel.showLanguageSelector.collectAsState()
                val isSelectingSource by viewModel.isSelectingSource.collectAsState()
                val sourceLang by viewModel.sourceLanguage.collectAsState()
                val targetLang by viewModel.targetLanguage.collectAsState()
                val showSettings by viewModel.showSettingsDialog.collectAsState()
                val speed by viewModel.speechSpeed.collectAsState()
                val pitch by viewModel.speechPitch.collectAsState()

                // Android System Navigation Gesture / Back Button Handler
                // Intercepts back gestures when not at root home screen or when dialogs/sheets are open
                val isBackNavigationActive = activeScreen != ActiveScreen.HOME || showLangSelector || showSettings
                BackHandler(enabled = isBackNavigationActive) {
                    viewModel.navigateBack()
                }

                // Permission launcher for microphone
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        // microphone permitted
                    }
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Show bottom nav bar on root tabs
                        if (activeScreen == ActiveScreen.HOME ||
                            activeScreen == ActiveScreen.LANGUAGES ||
                            activeScreen == ActiveScreen.PROFILE
                        ) {
                            BottomNavBar(
                                activeScreen = activeScreen,
                                onTabSelected = { screen ->
                                    viewModel.setActiveScreen(screen)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = activeScreen,
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                ActiveScreen.HOME -> {
                                    TranslateHomeScreen(
                                        viewModel = viewModel,
                                        onNavigateTo = { target ->
                                            viewModel.setActiveScreen(target)
                                        }
                                    )
                                }
                                ActiveScreen.LIVE_TRANSLATE -> {
                                    LiveTranslateScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                ActiveScreen.CONVERSATION -> {
                                    ConversationScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                ActiveScreen.HISTORY -> {
                                    HistoryScreen(
                                        viewModel = viewModel,
                                        onlyFavorites = false,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                ActiveScreen.SAVED -> {
                                    HistoryScreen(
                                        viewModel = viewModel,
                                        onlyFavorites = true,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                ActiveScreen.LANGUAGES -> {
                                    LanguagesScreen(viewModel = viewModel)
                                }
                                ActiveScreen.PROFILE -> {
                                    ProfileScreen(viewModel = viewModel)
                                }
                                ActiveScreen.SETTINGS -> {
                                    // Settings can also be opened via dialog
                                    TranslateHomeScreen(
                                        viewModel = viewModel,
                                        onNavigateTo = { target ->
                                            viewModel.setActiveScreen(target)
                                        }
                                    )
                                }
                            }
                        }

                        // Language Selector Sheet
                        if (showLangSelector) {
                            LanguageSelectorSheet(
                                isSelectingSource = isSelectingSource,
                                currentSelected = if (isSelectingSource) sourceLang else targetLang,
                                onLanguageSelected = { lang ->
                                    if (isSelectingSource) {
                                        viewModel.setSourceLanguage(lang)
                                    } else {
                                        viewModel.setTargetLanguage(lang)
                                    }
                                },
                                onDismiss = { viewModel.showLanguageSelector.value = false }
                            )
                        }

                        // Settings Dialog
                        if (showSettings) {
                            SettingsDialog(
                                speechSpeed = speed,
                                onSpeechSpeedChange = { viewModel.speechSpeed.value = it },
                                speechPitch = pitch,
                                onSpeechPitchChange = { viewModel.speechPitch.value = it },
                                onClearHistory = { viewModel.clearAllHistory() },
                                onDismiss = { viewModel.showSettingsDialog.value = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
