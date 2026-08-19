package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.model.ActiveScreen
import com.example.ui.screens.FaceToFaceScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LanguageLearningScreen
import com.example.ui.screens.LanguagePacksScreen
import com.example.ui.screens.LiveTranslationScreen
import com.example.ui.screens.TranslateHomeScreen
import com.example.ui.theme.AppCanvasBackground
import com.example.ui.theme.LiveTranslateTheme
import com.example.ui.viewmodel.TranslationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LiveTranslateTheme(darkTheme = true) {
                // Request Audio Recording permission
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handled
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

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.GlassCanvasDark
                ) {
                    val activeScreen by viewModel.activeScreen.collectAsState()

                    Crossfade(targetState = activeScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            ActiveScreen.TRANSLATE_HOME -> TranslateHomeScreen(viewModel = viewModel)
                            ActiveScreen.LIVE_SESSION -> LiveTranslationScreen(viewModel = viewModel)
                            ActiveScreen.FACE_TO_FACE -> FaceToFaceScreen(viewModel = viewModel)
                            ActiveScreen.HISTORY -> HistoryScreen(viewModel = viewModel)
                            ActiveScreen.LANGUAGE_PACKS -> LanguagePacksScreen(viewModel = viewModel)
                            ActiveScreen.LANGUAGE_LEARNING -> LanguageLearningScreen(viewModel = viewModel)
                            ActiveScreen.SETTINGS -> com.example.ui.screens.SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
