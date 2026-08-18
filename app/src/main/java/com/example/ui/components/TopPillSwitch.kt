package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ViewDisplayMode
import com.example.ui.theme.GlassBorderBottomLight
import com.example.ui.theme.GlassBorderTopLight
import com.example.ui.theme.GlassPillActiveBg
import com.example.ui.theme.GlassPillBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassSubtitle

@Composable
fun TopPillSwitch(
    selectedMode: ViewDisplayMode,
    onModeSelected: (ViewDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBilingual = selectedMode == ViewDisplayMode.BILINGUAL
    val bias by animateFloatAsState(
        targetValue = if (isBilingual) -1f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "pill_bias"
    )

    Box(
        modifier = modifier
            .width(204.dp)
            .height(36.dp)
            .shadow(4.dp, CircleShape, spotColor = Color(0x25000000))
            .clip(CircleShape)
            .background(GlassPillBg)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(GlassBorderTopLight, GlassBorderBottomLight)),
                shape = CircleShape
            )
            .padding(3.dp)
            .testTag("top_pill_switch"),
        contentAlignment = BiasAlignment(horizontalBias = bias, verticalBias = 0f)
    ) {
        // Sliding active frosted glass pill background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .shadow(4.dp, CircleShape, spotColor = NeonCyan.copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            GlassPillActiveBg,
                            Color(0x5500F0FF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            NeonCyan.copy(alpha = 0.8f),
                            Color(0xFF388BFF).copy(alpha = 0.5f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bilingual option
            val bilingualTextColor by animateColorAsState(
                targetValue = if (isBilingual) TextGlassHeading else TextGlassSubtitle,
                label = "bilingual_text_color"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onModeSelected(ViewDisplayMode.BILINGUAL) }
                    .testTag("pill_bilingual"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bilingual",
                    fontSize = 12.5.sp,
                    fontWeight = if (isBilingual) FontWeight.Bold else FontWeight.Medium,
                    color = bilingualTextColor,
                    letterSpacing = 0.1.sp
                )
            }

            // Translation option
            val transTextColor by animateColorAsState(
                targetValue = if (!isBilingual) TextGlassHeading else TextGlassSubtitle,
                label = "trans_text_color"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onModeSelected(ViewDisplayMode.TRANSLATION) }
                    .testTag("pill_translation"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Translation",
                    fontSize = 12.5.sp,
                    fontWeight = if (!isBilingual) FontWeight.Bold else FontWeight.Medium,
                    color = transTextColor,
                    letterSpacing = 0.1.sp
                )
            }
        }
    }
}
