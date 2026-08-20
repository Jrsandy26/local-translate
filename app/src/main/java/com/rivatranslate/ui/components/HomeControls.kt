package com.rivatranslate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivatranslate.model.Language
import com.rivatranslate.ui.theme.PurpleDark
import com.rivatranslate.ui.theme.PurpleGradientEnd
import com.rivatranslate.ui.theme.PurpleGradientStart
import com.rivatranslate.ui.theme.PurpleLight
import com.rivatranslate.ui.theme.PurplePrimary
import com.rivatranslate.ui.theme.TextMuted
import com.rivatranslate.ui.theme.TextPrimary
import com.rivatranslate.ui.theme.TextSecondary

@Composable
fun LanguagePillSelector(
    sourceLanguage: Language,
    targetLanguage: Language,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFF7F8FC))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Source
        Column(
            modifier = Modifier
                .clickable { onSourceClick() }
                .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = sourceLanguage.code.uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = sourceLanguage.name,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Swap Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PurpleLight)
                .clickable { onSwapClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = PurplePrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Target
        Column(
            modifier = Modifier
                .clickable { onTargetClick() }
                .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = targetLanguage.code.uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = targetLanguage.name,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun GlowingMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing halo
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(if (isListening) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PurplePrimary.copy(alpha = if (isListening) 0.35f else 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main Gradient Mic Button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                    )
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_btn_speak_now),
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
