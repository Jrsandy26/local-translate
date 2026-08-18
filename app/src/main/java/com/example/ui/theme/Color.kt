package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Glassmorphism Atmosphere & Background Colors
// ==========================================
val GlassCanvasDark = Color(0xFF0B0F19)
val GlassCanvasScrim = Color(0x350A0E1A)

// Ethereal Glow Orbs & Accents
val NeonCyan = Color(0xFF00F0FF)
val NeonBlue = Color(0xFF388BFF)
val NeonPurple = Color(0xFFA855F7)
val NeonMagenta = Color(0xFFEC4899)
val NeonEmerald = Color(0xFF10B981)
val NeonAmber = Color(0xFFF59E0B)

// Frosted Glass Card Backgrounds
val GlassCardSurface = Color(0x24FFFFFF)
val GlassCardSurfaceElevated = Color(0x33FFFFFF)
val GlassCardSurfaceSubtle = Color(0x14FFFFFF)
val GlassCardDarkBackdrop = Color(0x450F172A)

// Glass Borders (Beveled Specular Rim Highlights)
val GlassBorderTopLight = Color(0x80FFFFFF)
val GlassBorderBottomLight = Color(0x18FFFFFF)
val GlassBorderAccent = Color(0x40388BFF)
val GlassBorderCyan = Color(0x5000F0FF)

// Glass Pills & Chips
val GlassPillBg = Color(0x28FFFFFF)
val GlassPillActiveBg = Color(0x4A388BFF)
val GlassPillBorder = Color(0x40FFFFFF)
val GlassPillActiveBorder = Color(0x8000F0FF)

// Text Tokens for Ultra-Crisp Glass Readability
val TextGlassHeading = Color(0xFFFFFFFF)
val TextGlassBody = Color(0xFFE2E8F0)
val TextGlassSubtitle = Color(0xFF94A3B8)
val TextGlassMuted = Color(0xFF64748B)
val TextGlassHighlight = Color(0xFF38BDF8)

// Legacy Palette Support & Theme Integration
val AccentBlue = NeonBlue
val AccentCyan = NeonCyan
val AccentPurple = NeonPurple
val AccentGreen = NeonEmerald
val AccentAmber = NeonAmber
val AccentRed = Color(0xFFFF5252)

val DarkBackground = GlassCanvasDark
val DarkSurface = Color(0xFF131B2E)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkCardBorder = Color(0x33FFFFFF)
val DarkPillContainer = Color(0x22FFFFFF)
val DarkPillActive = Color(0x44388BFF)
val DarkDivider = Color(0x20FFFFFF)

val TextPrimaryDark = TextGlassHeading
val TextSecondaryDark = TextGlassSubtitle
val TextTertiaryDark = TextGlassMuted

val AppCanvasBackground = GlassCanvasDark
val CardBackground = GlassCardSurface
val CardBorderSubtle = GlassBorderBottomLight
val PrimaryVibrantBlue = NeonBlue
val PrimaryVibrantBlueHover = Color(0xFF2563EB)
val TextDarkHeading = TextGlassHeading
val TextDarkSubtitle = TextGlassSubtitle
val TextDarkMuted = TextGlassMuted
val ToggleActiveGreen = NeonEmerald
val ChipBg = GlassPillBg
val ChipBorderColor = GlassPillBorder

// ==========================================
// Glass Gradients Brushes
// ==========================================
fun glassCardGradient(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0x30FFFFFF),
        Color(0x12FFFFFF)
    )
)

fun glassElevatedGradient(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0x42FFFFFF),
        Color(0x20FFFFFF)
    )
)

fun glassRimBorderBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x99FFFFFF),
        Color(0x20FFFFFF),
        Color(0x6000F0FF),
        Color(0x15FFFFFF)
    )
)

fun glassActiveBorderBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00F0FF),
        Color(0xFF388BFF),
        Color(0xFFA855F7)
    )
)

fun glassAccentGlowGradient(): Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00C6FF),
        Color(0xFF0072FF)
    )
)

fun glassTextGradient(): Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFE0F2FE)
    )
)
