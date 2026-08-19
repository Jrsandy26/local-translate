package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExpressiveColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = NeonBlue,
    onSecondary = Color(0xFF002D6F),
    secondaryContainer = Color(0xFF00439A),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = NeonPurple,
    onTertiary = Color(0xFF4C007C),
    tertiaryContainer = Color(0xFF6B00AA),
    onTertiaryContainer = Color(0xFFF5E1FF),
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF131825),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E2435),
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceContainer = Color(0xFF171C2C),
    surfaceContainerHigh = Color(0xFF22283A),
    surfaceContainerHighest = Color(0xFF2C3348),
    outline = Color(0x40FFFFFF),
    outlineVariant = Color(0x20FFFFFF)
)

@Composable
fun LiveTranslateTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ExpressiveColorScheme,
        typography = Typography,
        content = content
    )
}
