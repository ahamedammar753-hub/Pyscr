package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DiagnosticAction
import com.example.domain.model.DiagnosticCheck
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaSuccessGreen
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack
import com.example.ui.theme.NovaWarningAmber

@Composable
fun DiagnosticsScreen(
    checks: List<DiagnosticCheck>,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("diagnostics_screen")
    ) {
        NovaTopBar(
            title = "Permissions & Diagnostics",
            subtitle = "Security environment & privilege verification",
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("btn_refresh_diagnostics")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = NovaCyanNeon
                    )
                }
            }
        )

        Text(
            text = "SYSTEM PERMISSIONS & SUBSYSTEM AUDIT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            checks.forEach { check ->
                NovaCard(
                    borderColor = if (check.isPassed) NovaSuccessGreen.copy(alpha = 0.4f) else NovaWarningAmber.copy(alpha = 0.4f),
                    modifier = Modifier.testTag("diagnostic_item_${check.id}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (check.isPassed) NovaSuccessGreen.copy(alpha = 0.15f) else NovaWarningAmber.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (check.isPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (check.isPassed) NovaSuccessGreen else NovaWarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = check.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NovaTextPrimary
                                )
                                Text(
                                    text = check.subtitle,
                                    fontSize = 11.sp,
                                    color = NovaTextSecondary
                                )
                            }

                            NovaStatusBadge(
                                text = if (check.isPassed) "PASSED" else if (check.isRequired) "REQUIRED" else "OPTIONAL",
                                isSuccess = check.isPassed,
                                isWarning = !check.isPassed
                            )
                        }

                        if (!check.isPassed && check.actionType == DiagnosticAction.SYSTEM_ALERT_WINDOW) {
                            Spacer(modifier = Modifier.height(12.dp))
                            NovaButton(
                                text = "Grant 'Display over other apps' Permission",
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                        } catch (e2: Exception) {
                                            e2.printStackTrace()
                                        }
                                    }
                                },
                                isSecondary = true,
                                modifier = Modifier.fillMaxWidth(),
                                tag = "btn_grant_overlay"
                            )
                        } else if (!check.isPassed && check.actionType == DiagnosticAction.NOTIFICATION_LISTENER) {
                            Spacer(modifier = Modifier.height(12.dp))
                            NovaButton(
                                text = "Grant Notification Access Permission",
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                isSecondary = true,
                                modifier = Modifier.fillMaxWidth(),
                                tag = "btn_grant_notif"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
