package com.rivatranslate.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Primary Branding Colors
val PurplePrimary = Color(0xFF6C5CE7)
val PurpleDark = Color(0xFF5843D8)
val PurpleLight = Color(0xFFEDE9FE)
val PurpleGradientStart = Color(0xFF5E4AE3)
val PurpleGradientEnd = Color(0xFF917BFF)

// Dark Primary Branding
val PurplePrimaryDark = Color(0xFF8A7BFF)
val PurpleDarkDark = Color(0xFF705CE7)
val PurpleLightDark = Color(0xFF2E2752)
val PurpleGradientStartDark = Color(0xFF6D56E8)
val PurpleGradientEndDark = Color(0xFFA594FF)

// Neutral Text Colors (Light)
val TextPrimary = Color(0xFF1E1B2E)
val TextSecondary = Color(0xFF75758B)
val TextMuted = Color(0xFFA0A0B2)

// Neutral Text Colors (Dark)
val TextPrimaryDark = Color(0xFFF5F4FA)
val TextSecondaryDark = Color(0xFFA8A5C2)
val TextMutedDark = Color(0xFF737090)

// Canvas & Surface (Light)
val CardBackground = Color(0xFFFFFFFF)
val AppBackground = Color(0xFFF7F8FC)
val BorderLight = Color(0xFFEEF0F8)
val PillBackground = Color(0xFFF1F1F8)

// Canvas & Surface (Dark)
val CardBackgroundDark = Color(0xFF1D1B2E)
val CardElevatedDark = Color(0xFF25223A)
val AppBackgroundDark = Color(0xFF11101D)
val BorderLightDark = Color(0xFF2B2842)
val PillBackgroundDark = Color(0xFF262438)

// Functional Accent Colors (Light & Dark)
val AccentLive = Color(0xFF7C4DFF)
val AccentLiveBg = Color(0xFFF1EDFF)
val AccentLiveBgDark = Color(0xFF2B224F)

val AccentConversation = Color(0xFF10B981)
val AccentConversationBg = Color(0xFFE6F7F0)
val AccentConversationBgDark = Color(0xFF1A332B)

val AccentHistory = Color(0xFF3B82F6)
val AccentHistoryBg = Color(0xFFEFF6FF)
val AccentHistoryBgDark = Color(0xFF1C2C46)

val AccentSaved = Color(0xFFF59E0B)
val AccentSavedBg = Color(0xFFFEF3C7)
val AccentSavedBgDark = Color(0xFF3D3018)

val StarActive = Color(0xFFF59E0B)

// Live Translate Recording Screen Theme Colors
@Immutable
data class LiveThemePalette(
    val screenBackground: Color,
    val cardBackground: Color,
    val pillBackground: Color,
    val pillBorder: Color,
    val titleText: Color,
    val subtitleText: Color,
    val accentOrange: Color,
    val accentOrangeLight: Color,
    val controlCardBg: Color,
    val controlCircleBg: Color,
    val dottedLine: Color,
    val isDark: Boolean
)

val LightLiveTheme = LiveThemePalette(
    screenBackground = Color(0xFFFFF9F3),
    cardBackground = Color(0xFFFFF8F1),
    pillBackground = Color(0xFFF6ECE0),
    pillBorder = Color(0xFFEEDECF),
    titleText = Color(0xFF2C1D13),
    subtitleText = Color(0xFF8C7362),
    accentOrange = Color(0xFFEE7931),
    accentOrangeLight = Color(0xFFFFE5D6),
    controlCardBg = Color(0xFFFDF5EC),
    controlCircleBg = Color(0xFFF6E8DC),
    dottedLine = Color(0xFFE8D7C7),
    isDark = false
)

val DarkLiveTheme = LiveThemePalette(
    screenBackground = Color(0xFF110F19),
    cardBackground = Color(0xFF1C1929),
    pillBackground = Color(0xFF2A253C),
    pillBorder = Color(0xFF3B3454),
    titleText = Color(0xFFF7F5FE),
    subtitleText = Color(0xFFAAA5C8),
    accentOrange = Color(0xFFFF8F5A),
    accentOrangeLight = Color(0xFF402216),
    controlCardBg = Color(0xFF181524),
    controlCircleBg = Color(0xFF28233B),
    dottedLine = Color(0xFF3D3754),
    isDark = true
)
