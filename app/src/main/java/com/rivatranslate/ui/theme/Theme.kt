package com.rivatranslate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rivatranslate.model.AppThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = PillBackground,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PurpleLightDark,
    onPrimaryContainer = Color(0xFFD6CFFF),
    background = AppBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = CardBackgroundDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = PillBackgroundDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderLightDark
)

@Immutable
data class ExtendedColors(
    val isDark: Boolean,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val pillBackground: Color,
    val accentLive: Color,
    val accentLiveBg: Color,
    val accentConversation: Color,
    val accentConversationBg: Color,
    val accentHistory: Color,
    val accentHistoryBg: Color,
    val accentSaved: Color,
    val accentSavedBg: Color,
    val liveTheme: LiveThemePalette
)

val LightExtendedColors = ExtendedColors(
    isDark = false,
    primary = PurplePrimary,
    primaryDark = PurpleDark,
    primaryLight = PurpleLight,
    gradientStart = PurpleGradientStart,
    gradientEnd = PurpleGradientEnd,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    background = AppBackground,
    surface = CardBackground,
    surfaceElevated = Color.White,
    border = BorderLight,
    pillBackground = PillBackground,
    accentLive = AccentLive,
    accentLiveBg = AccentLiveBg,
    accentConversation = AccentConversation,
    accentConversationBg = AccentConversationBg,
    accentHistory = AccentHistory,
    accentHistoryBg = AccentHistoryBg,
    accentSaved = AccentSaved,
    accentSavedBg = AccentSavedBg,
    liveTheme = LightLiveTheme
)

val DarkExtendedColors = ExtendedColors(
    isDark = true,
    primary = PurplePrimaryDark,
    primaryDark = PurpleDarkDark,
    primaryLight = PurpleLightDark,
    gradientStart = PurpleGradientStartDark,
    gradientEnd = PurpleGradientEndDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    background = AppBackgroundDark,
    surface = CardBackgroundDark,
    surfaceElevated = CardElevatedDark,
    border = BorderLightDark,
    pillBackground = PillBackgroundDark,
    accentLive = Color(0xFFA78BFA),
    accentLiveBg = AccentLiveBgDark,
    accentConversation = Color(0xFF34D399),
    accentConversationBg = AccentConversationBgDark,
    accentHistory = Color(0xFF60A5FA),
    accentHistoryBg = AccentHistoryBgDark,
    accentSaved = Color(0xFFFBBF24),
    accentSavedBg = AccentSavedBgDark,
    liveTheme = DarkLiveTheme
)

val LocalAppColors = staticCompositionLocalOf { LightExtendedColors }

object AppTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val liveTheme: LiveThemePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.liveTheme
}

@Composable
fun RivaTranslateTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemInDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val extendedColors = if (isDark) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
