package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.*

@Composable
fun DropdownLanguageSelector(
    sourceLanguage: Language,
    targetLanguage: Language,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onSwapLanguages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LanguageDropdown(
            selectedLanguage = sourceLanguage,
            onLanguageSelected = onSourceLanguageSelected,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(
            onClick = onSwapLanguages,
            modifier = Modifier
                .size(36.dp)
                .background(ChipBg, RoundedCornerShape(12.dp))
                .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = PrimaryVibrantBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        LanguageDropdown(
            selectedLanguage = targetLanguage,
            onLanguageSelected = onTargetLanguageSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LanguageDropdown(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${selectedLanguage.flag} ${selectedLanguage.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDarkHeading,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Select Language",
                tint = TextDarkSubtitle
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(CardBackground)
                .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
        ) {
            Language.SUPPORTED_LANGUAGES.forEach { lang ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "${lang.flag} ${lang.name}",
                            color = TextDarkHeading,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onLanguageSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}
