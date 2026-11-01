package com.example.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PrivacyEventEntity
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDangerRed
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaSuccessGreen
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack
import com.example.ui.theme.NovaWarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    events: List<PrivacyEventEntity>,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .testTag("history_screen")
    ) {
        NovaTopBar(
            title = "Privacy Activity History",
            subtitle = "${events.size} audit records logged locally",
            onBack = onBack,
            actions = {
                if (events.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("btn_clear_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = NovaDangerRed
                        )
                    }
                }
            }
        )

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = NovaTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Privacy Events Recorded",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = "Privacy audit events and shield changes will appear here.",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    val formattedTime = remember(event.timestamp) {
                        val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                        sdf.format(Date(event.timestamp))
                    }

                    NovaCard(
                        borderColor = NovaDarkCardBorder,
                        modifier = Modifier.testTag("event_item_${event.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (event.severity) {
                                            "SUCCESS" -> NovaSuccessGreen
                                            "SECURITY" -> NovaElectricViolet
                                            "WARNING" -> NovaWarningAmber
                                            else -> NovaCyanNeon
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NovaTextPrimary
                                    )
                                    Text(
                                        text = formattedTime,
                                        fontSize = 10.sp,
                                        color = NovaTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.description,
                                    fontSize = 12.sp,
                                    color = NovaTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Audit Log?", color = NovaTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all stored privacy activity records from local database.", color = NovaTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    modifier = Modifier.testTag("dialog_confirm_clear_history")
                ) {
                    Text("Clear All", color = NovaDangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            },
            containerColor = NovaVoidBlack
        )
    }
}
