package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.model.SessionWithSegments
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.theme.TextGlassSubtitle
import com.example.ui.viewmodel.TranslationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: TranslationViewModel) {
    val allSessions by viewModel.allSessions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var renameSessionId by remember { mutableStateOf<Long?>(null) }
    var renameTitleInput by remember { mutableStateOf("") }

    val filteredSessions = remember(allSessions, searchQuery) {
        if (searchQuery.isBlank()) {
            allSessions
        } else {
            allSessions.filter {
                it.session.title.contains(searchQuery, ignoreCase = true) ||
                        it.session.sourceLanguageCode.contains(searchQuery, ignoreCase = true) ||
                        it.session.targetLanguageCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        size = 36.dp,
                        onClick = { viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "History",
                        color = TextGlassHeading,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${filteredSessions.size} Sessions",
                    color = TextGlassMuted,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search sessions...", color = TextGlassMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextGlassMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0x30FFFFFF),
                    focusedContainerColor = Color(0x10FFFFFF),
                    unfocusedContainerColor = Color(0x05FFFFFF),
                    focusedTextColor = TextGlassHeading,
                    unfocusedTextColor = TextGlassBody
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sessions List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredSessions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No translation sessions found.",
                                color = TextGlassMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(filteredSessions) { item ->
                    SessionItemRow(
                        item = item,
                        onOpen = { viewModel.openSession(item.session.id) },
                        onRename = {
                            renameSessionId = item.session.id
                            renameTitleInput = item.session.title
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(item.session.id, item.session.isFavorite) },
                        onDelete = { viewModel.deleteSession(item.session.id) }
                    )
                }
            }
        }
    }

    // Rename Session Dialog
    if (renameSessionId != null) {
        AlertDialog(
            onDismissRequest = { renameSessionId = null },
            title = { Text("Rename Session", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameTitleInput,
                    onValueChange = { renameTitleInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sessId = renameSessionId
                        if (sessId != null) {
                            viewModel.renameSession(sessId, renameTitleInput)
                        }
                        renameSessionId = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                Button(onClick = { renameSessionId = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun SessionItemRow(
    item: SessionWithSegments,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val dateString = formatter.format(Date(item.session.timestamp))

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isElevated = false,
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.session.title,
                    color = TextGlassHeading,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.session.sourceLanguageCode.uppercase(Locale.ROOT)} → ${item.session.targetLanguageCode.uppercase(Locale.ROOT)}  •  ${item.segments.size} sentences  •  $dateString",
                    color = TextGlassMuted,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GlassIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Rename Session",
                    size = 32.dp,
                    iconSize = 15.dp,
                    onClick = onRename
                )
                GlassIconButton(
                    icon = if (item.session.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    size = 32.dp,
                    iconSize = 15.dp,
                    tint = if (item.session.isFavorite) Color(0xFFEF4444) else TextGlassHeading,
                    onClick = onFavoriteToggle
                )
                GlassIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    size = 32.dp,
                    iconSize = 15.dp,
                    tint = Color(0xFFF87171),
                    onClick = onDelete
                )
            }
        }
    }
}
