package com.example.presentation.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
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
import com.example.domain.model.ProtectedApp
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.presentation.components.SettingToggleRow
import com.example.presentation.viewmodel.NovaUiState
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
fun NotificationSettingsScreen(
    state: NovaUiState,
    allApps: List<ProtectedApp>,
    onToggleHideSensitive: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isGranted = state.isNotificationListenerGranted
    val isHidingActive = state.profileConfig.hideSensitiveNotifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("notification_settings_screen")
    ) {
        NovaTopBar(
            title = "Notification Privacy",
            subtitle = "Mask incoming alerts from curious eyes",
            onBack = onBack
        )

        // Status Card
        Box(modifier = Modifier.padding(16.dp)) {
            NovaCard(borderColor = if (isGranted) NovaSuccessGreen else NovaWarningAmber) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) NovaSuccessGreen.copy(alpha = 0.15f) else NovaWarningAmber.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (isGranted) NovaSuccessGreen else NovaWarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isGranted) "Notification Access Active" else "Notification Access Required",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = if (isGranted) "Nova listener monitors alerts from protected apps" else "Grant notification listener access in Android Settings",
                            fontSize = 12.sp,
                            color = NovaTextSecondary
                        )
                    }

                    NovaStatusBadge(
                        text = if (isGranted) "AUTHORIZED" else "PERMISSION NEEDED",
                        isSuccess = isGranted,
                        isWarning = !isGranted
                    )
                }
            }
        }

        if (!isGranted) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                NovaButton(
                    text = "Authorize Notification Access in Settings",
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    tag = "btn_grant_notif_perm"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notification Controls
        Text(
            text = "PREVIEW MASKING RULES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard {
                Column {
                    SettingToggleRow(
                        title = "Mask Sensitive Notification Previews",
                        description = "Obscures sender name and message content from protected apps on the lock screen and heads-up popups",
                        isChecked = isHidingActive,
                        onCheckedChange = onToggleHideSensitive,
                        icon = Icons.Default.Security,
                        tag = "toggle_hide_notifs"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Settings Guidance
        Text(
            text = "SYSTEM-LEVEL LOCK SCREEN PRIVACY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard(borderColor = NovaDarkCardBorder) {
                Column {
                    Text(
                        text = "RECOMMENDED ANDROID SYSTEM SETTING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaCyanNeon,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "For maximum protection, Android allows you to hide sensitive content globally:\n\n1. Open Android System Settings → Notifications\n2. Select 'Notifications on lock screen'\n3. Choose 'Hide sensitive content' or 'Don't show notifications'\n\nThis guarantees that Android OS itself stops displaying text snippets before unlocking.",
                        fontSize = 13.sp,
                        color = NovaTextSecondary,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
