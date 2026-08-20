package com.rivatranslate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import com.rivatranslate.model.ActiveScreen
import com.rivatranslate.ui.components.BottomNavBar
import com.rivatranslate.ui.components.LanguageSelectorSheet
import com.rivatranslate.ui.components.SettingsDialog
import com.rivatranslate.ui.screens.*
import com.rivatranslate.ui.theme.RivaTranslateTheme
import com.rivatranslate.ui.viewmodel.TranslationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            RivaTranslateTheme(themeMode = themeMode) {
                val activeScreen by viewModel.activeScreen.collectAsState()
                val showLangSelector by viewModel.showLanguageSelector.collectAsState()
                val isSelectingSource by viewModel.isSelectingSource.collectAsState()
                val sourceLang by viewModel.sourceLanguage.collectAsState()
                val targetLang by viewModel.targetLanguage.collectAsState()
                val showSettings by viewModel.showSettingsDialog.collectAsState()
                val speed by viewModel.speechSpeed.collectAsState()
                val pitch by viewModel.speechPitch.collectAsState()
                val preferredVoiceGender by viewModel.preferredVoiceGender.collectAsState()

                // Android System Navigation Gesture / Back Button Handler
                // Intercepts back gestures when not at root home screen or when dialogs/sheets are open
                val isBackNavigationActive = activeScreen != ActiveScreen.HOME || showLangSelector || showSettings
                BackHandler(enabled = isBackNavigationActive) {
                    viewModel.navigateBack()
                }

                // Permission launcher for microphone and live notifications
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (permissionsToRequest.any {
                            ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                        }
                    ) {
                        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
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
                                themeMode = themeMode,
                                onThemeModeChange = { viewModel.setThemeMode(it) },
                                speechSpeed = speed,
                                onSpeechSpeedChange = { viewModel.updateSpeechSpeed(it) },
                                speechPitch = pitch,
                                onSpeechPitchChange = { viewModel.updateSpeechPitch(it) },
                                preferredVoiceGender = preferredVoiceGender,
                                onVoiceGenderChange = { viewModel.updateVoiceGender(it) },
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
