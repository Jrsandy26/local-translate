package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.ui.theme.AppTheme

@Composable
fun BottomNavBar(
    activeScreen: ActiveScreen,
    onTabSelected: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (colors.isDark) 4.dp else 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = colors.primary.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(colors.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = activeScreen == ActiveScreen.HOME,
                onClick = { onTabSelected(ActiveScreen.HOME) },
                testTag = "nav_home"
            )

            NavItem(
                label = "Languages",
                icon = Icons.Default.Language,
                isSelected = activeScreen == ActiveScreen.LANGUAGES,
                onClick = { onTabSelected(ActiveScreen.LANGUAGES) },
                testTag = "nav_languages"
            )

            NavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                isSelected = activeScreen == ActiveScreen.PROFILE,
                onClick = { onTabSelected(ActiveScreen.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = AppTheme.colors

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.primaryLight else Color.Transparent,
        label = "navBgColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.textMuted,
        label = "navContentColor"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}
