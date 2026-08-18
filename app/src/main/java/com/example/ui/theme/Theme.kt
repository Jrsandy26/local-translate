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

private val GlassColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF030D1A),
    primaryContainer = GlassPillActiveBg,
    onPrimaryContainer = Color.White,
    secondary = NeonBlue,
    onSecondary = Color.White,
    secondaryContainer = GlassCardSurface,
    onSecondaryContainer = Color.White,
    tertiary = NeonEmerald,
    onTertiary = Color(0xFF021B10),
    background = GlassCanvasDark,
    onBackground = TextGlassHeading,
    surface = GlassCardSurface,
    onSurface = TextGlassHeading,
    surfaceVariant = GlassCardSurfaceElevated,
    onSurfaceVariant = TextGlassSubtitle,
    outline = GlassBorderBottomLight,
    outlineVariant = GlassBorderTopLight
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
        colorScheme = GlassColorScheme,
        typography = Typography,
        content = content
    )
}
