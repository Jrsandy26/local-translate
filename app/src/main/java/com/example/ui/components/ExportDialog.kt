package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExportFormat
import com.example.ui.theme.GlassBorderBottomLight
import com.example.ui.theme.GlassBorderTopLight
import com.example.ui.theme.GlassCardSurface
import com.example.ui.theme.GlassCardSurfaceElevated
import com.example.ui.theme.GlassPillActiveBg
import com.example.ui.theme.GlassPillBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.theme.TextGlassSubtitle

@Composable
fun ExportDialog(
    sessionTitle: String,
    onExport: (ExportFormat, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var includeTimestamps by remember { mutableStateOf(true) }
    var includeSourceText by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Column {
                Text(
                    text = "Export Transcript",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGlassHeading
                )
                Text(
                    text = sessionTitle,
                    fontSize = 12.5.sp,
                    color = TextGlassSubtitle,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DOCUMENT FORMAT",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Format Selector Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormatCard(
                        format = ExportFormat.PDF,
                        isSelected = selectedFormat == ExportFormat.PDF,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormat = ExportFormat.PDF }
                    )
                    ExportFormatCard(
                        format = ExportFormat.WORD,
                        isSelected = selectedFormat == ExportFormat.WORD,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormat = ExportFormat.WORD }
                    )
                    ExportFormatCard(
                        format = ExportFormat.TEXT,
                        isSelected = selectedFormat == ExportFormat.TEXT,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormat = ExportFormat.TEXT }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EXPORT OPTIONS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Checkboxes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassPillBg)
                        .border(1.dp, GlassBorderBottomLight, RoundedCornerShape(10.dp))
                        .clickable { includeTimestamps = !includeTimestamps }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeTimestamps,
                        onCheckedChange = { includeTimestamps = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            uncheckedColor = TextGlassMuted,
                            checkmarkColor = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Include audio timestamps",
                        fontSize = 12.5.sp,
                        color = TextGlassBody
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassPillBg)
                        .border(1.dp, GlassBorderBottomLight, RoundedCornerShape(10.dp))
                        .clickable { includeSourceText = !includeSourceText }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeSourceText,
                        onCheckedChange = { includeSourceText = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            uncheckedColor = TextGlassMuted,
                            checkmarkColor = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Include original source speech",
                        fontSize = 12.5.sp,
                        color = TextGlassBody
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(selectedFormat, includeTimestamps, includeSourceText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = NeonCyan.copy(alpha = 0.5f))
                    .testTag("export_confirm_button")
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export & Share", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGlassSubtitle, fontSize = 13.5.sp)
            }
        },
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun ExportFormatCard(
    format: ExportFormat,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val icon = when (format) {
        ExportFormat.PDF -> Icons.Filled.PictureAsPdf
        ExportFormat.WORD -> Icons.Filled.Description
        ExportFormat.TEXT -> Icons.Filled.TextFields
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 4.dp else 0.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (isSelected) NeonCyan.copy(alpha = 0.4f) else Color.Transparent
            )
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) GlassPillActiveBg else GlassCardSurface)
            .border(
                width = if (isSelected) 1.2.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(listOf(NeonCyan, Color(0xFF388BFF)))
                } else {
                    Brush.linearGradient(listOf(GlassBorderTopLight, GlassBorderBottomLight))
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = format.title,
                tint = if (isSelected) NeonCyan else TextGlassSubtitle,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = format.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NeonCyan else TextGlassHeading
            )
        }
    }
}
