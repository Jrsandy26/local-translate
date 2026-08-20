package com.rivatranslate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivatranslate.model.RecentTranslation
import com.rivatranslate.ui.theme.PurpleLight
import com.rivatranslate.ui.theme.PurplePrimary
import com.rivatranslate.ui.theme.StarActive
import com.rivatranslate.ui.theme.TextMuted
import com.rivatranslate.ui.theme.TextPrimary
import com.rivatranslate.ui.theme.TextSecondary

@Composable
fun RecentTranslationCard(
    item: RecentTranslation,
    onToggleFavorite: () -> Unit,
    onSpeak: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle Avatar with Lang Code
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.sourceLangCode.uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Text & Translation
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.sourceText.ifEmpty { "No text" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.translatedText.ifEmpty { "No translation" },
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }

        // Star Favorite Button
        IconButton(
            onClick = { onToggleFavorite() },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorite",
                tint = if (item.isFavorite) StarActive else Color(0xFF9E9EBA),
                modifier = Modifier.size(22.dp)
            )
        }

        // More Options Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color(0xFF9E9EBA),
                    modifier = Modifier.size(22.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Speak") },
                    onClick = {
                        showMenu = false
                        onSpeak()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
