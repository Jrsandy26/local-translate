package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun BottomNavBar(
    activeScreen: ActiveScreen,
    onTabSelected: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0x1A6C5CE7)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = activeScreen == ActiveScreen.HOME,
                onClick = { onTabSelected(ActiveScreen.HOME) }
            )

            NavItem(
                label = "Languages",
                icon = Icons.Default.Language,
                isSelected = activeScreen == ActiveScreen.LANGUAGES,
                onClick = { onTabSelected(ActiveScreen.LANGUAGES) }
            )

            NavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                isSelected = activeScreen == ActiveScreen.PROFILE,
                onClick = { onTabSelected(ActiveScreen.PROFILE) }
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PurpleLight else Color.Transparent,
        label = "bgColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PurplePrimary else TextMuted,
        label = "contentColor"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
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
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
