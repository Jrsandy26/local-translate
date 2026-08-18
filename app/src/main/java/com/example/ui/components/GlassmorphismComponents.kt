package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GlassBorderAccent
import com.example.ui.theme.GlassBorderBottomLight
import com.example.ui.theme.GlassBorderTopLight
import com.example.ui.theme.GlassCardDarkBackdrop
import com.example.ui.theme.GlassCardSurface
import com.example.ui.theme.GlassCardSurfaceElevated
import com.example.ui.theme.GlassPillActiveBg
import com.example.ui.theme.GlassPillActiveBorder
import com.example.ui.theme.GlassPillBg
import com.example.ui.theme.GlassPillBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading

/**
 * Atmospheric Glassmorphism Canvas Background
 * Combines ethereal wallpaper mesh with animated luminous ambient floating orbs
 * and a deep dark glass vignette scrim for high-contrast readability.
 */
@Composable
fun GlassAtmosphereBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_bg_orbs")
    val orbAnim1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val orbAnim2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -70f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // Base Wallpaper Mesh with Frosted Depth
        Image(
            painter = painterResource(id = R.drawable.img_glass_mesh_bg_1786959335929),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.55f
        )

        // Animated Ambient Glowing Liquid Canvas Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Top-Right Cyan Glow Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.28f),
                        NeonCyan.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.85f + orbAnim1, canvasH * 0.15f + orbAnim2),
                    radius = canvasW * 0.65f
                )
            )

            // Center-Left Deep Violet Glow Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonPurple.copy(alpha = 0.25f),
                        Color(0xFF3B82F6).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.15f - orbAnim2, canvasH * 0.45f + orbAnim1),
                    radius = canvasW * 0.75f
                )
            )

            // Bottom-Right Neon Magenta Accent Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonMagenta.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.8f + orbAnim2, canvasH * 0.82f - orbAnim1),
                    radius = canvasW * 0.7f
                )
            )
        }

        // Frosted Ambient Dark Scrim for High-Contrast Readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x35060A13),
                            Color(0x55060A13),
                            Color(0x65060A13)
                        )
                    )
                )
        )

        // Content Layer
        content()
    }
}

/**
 * Frosted Glass Card with Specular Gradient Rim Light Border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    isElevated: Boolean = false,
    isActive: Boolean = false,
    elevation: Dp = if (isElevated) 8.dp else 4.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = if (isActive) {
        Brush.linearGradient(
            colors = listOf(
                NeonCyan.copy(alpha = 0.9f),
                Color(0xFF388BFF).copy(alpha = 0.6f),
                NeonPurple.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GlassBorderTopLight,
                GlassBorderBottomLight,
                GlassBorderAccent.copy(alpha = 0.25f),
                GlassBorderBottomLight
            )
        )
    }

    val backgroundBrush = if (isElevated) {
        Brush.verticalGradient(
            colors = listOf(
                GlassCardSurfaceElevated,
                GlassCardSurface
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                GlassCardSurface,
                Color(0x18FFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isActive) NeonCyan.copy(alpha = 0.35f) else Color(0x28000000),
                ambientColor = Color(0x18000000)
            )
            .clip(shape)
            .background(GlassCardDarkBackdrop)
            .background(backgroundBrush)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = NeonCyan.copy(alpha = 0.3f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .padding(if (isElevated) 18.dp else 16.dp),
        content = content
    )
}

/**
 * Frosted Glass Pill Capsule
 */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                GlassPillActiveBg,
                Color(0x3500F0FF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GlassPillBg,
                Color(0x15FFFFFF)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                GlassPillActiveBorder,
                Color(0xFF388BFF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GlassPillBorder,
                Color(0x20FFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = shape,
                spotColor = if (isSelected) NeonCyan.copy(alpha = 0.4f) else Color.Transparent
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (isSelected) 1.2.dp else 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = NeonCyan.copy(alpha = 0.25f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Frosted Glass Circular Icon Button
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = TextGlassHeading,
    size: Dp = 38.dp,
    iconSize: Dp = 19.dp,
    isActive: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isActive) 4.dp else 2.dp,
                shape = shape,
                spotColor = if (isActive) NeonCyan.copy(alpha = 0.35f) else Color(0x20000000)
            )
            .clip(shape)
            .background(if (isActive) GlassPillActiveBg else GlassCardSurface)
            .border(
                width = 1.dp,
                brush = if (isActive) {
                    Brush.linearGradient(listOf(NeonCyan, Color(0xFF388BFF)))
                } else {
                    Brush.linearGradient(listOf(GlassBorderTopLight, GlassBorderBottomLight))
                },
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = NeonCyan.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Tactile Frosted Glass Gradient Button
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    gradientColors: List<Color> = listOf(NeonCyan, Color(0xFF0066FF)),
    testTag: String = ""
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                spotColor = gradientColors.first().copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(Brush.linearGradient(gradientColors))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                onClick = onClick
            )
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
        }
    }
}
