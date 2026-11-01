package com.example.presentation.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PrivacyEventEntity
import com.example.domain.model.PrivacyProfile
import com.example.domain.model.ProtectedApp
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaShieldBadge
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.navigation.NovaScreen
import com.example.presentation.viewmodel.NovaUiState
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCard
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaSuccessGreen
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack
import com.example.ui.theme.NovaWarningAmber

@Composable
fun DashboardScreen(
    state: NovaUiState,
    allApps: List<ProtectedApp>,
    recentEvents: List<PrivacyEventEntity>,
    onNavigate: (NovaScreen) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    onTogglePeephole: (Boolean) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onSelectProfile: (PrivacyProfile) -> Unit
) {
    val scrollState = rememberScrollState()
    val protectedCount = allApps.count { it.isProtected }
    val totalCount = allApps.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
            .testTag("dashboard_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NOVA SCREEN GUARD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = NovaCyanNeon
                )
                Text(
                    text = "Privacy Command",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NovaTextPrimary
                )
            }

            IconButton(
                onClick = { onNavigate(NovaScreen.DIAGNOSTICS) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NovaDarkCard)
                    .testTag("dashboard_diagnostics_icon")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Diagnostics",
                    tint = NovaCyanNeon
                )
            }
        }

        // Center Pulsing Shield Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            NovaShieldBadge(
                score = state.privacyScore,
                isProtected = state.isOverlayActive || protectedCount > 0,
                statusText = state.privacyStatus.label
            )
        }

        // Quick Active Profile Card
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
            NovaCard(
                borderColor = NovaElectricViolet.copy(alpha = 0.5f),
                onClick = { onNavigate(NovaScreen.PROFILES) },
                modifier = Modifier.testTag("dashboard_profile_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NovaElectricViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NovaCyanNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "ACTIVE PROFILE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = NovaCyanNeon
                            )
                            Text(
                                text = state.activeProfile.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = state.activeProfile.subtitle,
                                fontSize = 12.sp,
                                color = NovaTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Change Profile",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Hardware Compatibility Status Pill Card
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
            val hasHw = state.hardwareReport?.hasHardwarePrivacyDisplay == true
            NovaCard(
                borderColor = if (hasHw) NovaSuccessGreen.copy(alpha = 0.6f) else NovaDarkCardBorder,
                onClick = { onNavigate(NovaScreen.HARDWARE_COMPAT) },
                modifier = Modifier.testTag("dashboard_hardware_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (hasHw) NovaSuccessGreen.copy(alpha = 0.2f) else NovaWarningAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DisplaySettings,
                            contentDescription = null,
                            tint = if (hasHw) NovaSuccessGreen else NovaWarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DISPLAY HARDWARE STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasHw) NovaSuccessGreen else NovaWarningAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (hasHw) "Hardware Privacy Display Active" else "Standard Display Panel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = if (hasHw) "API: ${state.hardwareReport?.detectedApiName}" else "Viewing angles wide • Software Dimmer Available",
                            fontSize = 11.sp,
                            color = NovaTextSecondary
                        )
                    }
                    NovaStatusBadge(
                        text = if (hasHw) "HARDWARE" else "SOFTWARE",
                        isSuccess = hasHw,
                        isWarning = !hasHw
                    )
                }
            }
        }

        // Main Privacy Controls Section
        Text(
            text = "LIVE SCREEN CONTROLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )

        // Screen Shield Overlay Master Switch Card
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
            NovaCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NovaCyanNeon.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isOverlayActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = NovaCyanNeon,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Stealth Screen Shield",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NovaTextPrimary
                                )
                                Text(
                                    text = if (state.isOverlayActive) "Dimmer active over running apps" else "Screen at normal brightness",
                                    fontSize = 12.sp,
                                    color = NovaTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = state.isOverlayActive,
                            onCheckedChange = onToggleOverlay,
                            modifier = Modifier.testTag("dashboard_overlay_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaVoidBlack,
                                checkedTrackColor = NovaCyanNeon
                            )
                        )
                    }

                    if (state.isOverlayActive) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Opacity slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Dimmer Intensity", fontSize = 12.sp, color = NovaTextSecondary)
                            Text(text = "${(state.overlayOpacity * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NovaCyanNeon)
                        }
                        Slider(
                            value = state.overlayOpacity,
                            onValueChange = onOpacityChange,
                            valueRange = 0.20f..0.90f,
                            colors = SliderDefaults.colors(
                                thumbColor = NovaCyanNeon,
                                activeTrackColor = NovaCyanNeon,
                                inactiveTrackColor = NovaDarkCardBorder
                            ),
                            modifier = Modifier.testTag("dashboard_opacity_slider")
                        )

                        // Peephole Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Peephole Focus Slit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                                Text(text = "Keep a horizontal slit clear, heavily obscure edges", fontSize = 11.sp, color = NovaTextSecondary)
                            }
                            Switch(
                                checked = state.isPeepholeActive,
                                onCheckedChange = onTogglePeephole,
                                modifier = Modifier.testTag("dashboard_peephole_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NovaVoidBlack,
                                    checkedTrackColor = NovaElectricViolet
                                )
                            )
                        }
                    }
                }
            }
        }

        // Navigation Grid Section
        Text(
            text = "GUARD MODULES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Protected Apps Card
            DashboardNavRow(
                icon = Icons.Default.Apps,
                title = "Protected Apps",
                subtitle = "$protectedCount of $totalCount apps guarded",
                badgeText = "$protectedCount ACTIVE",
                isSuccess = protectedCount > 0,
                tag = "nav_protected_apps",
                onClick = { onNavigate(NovaScreen.PROTECTED_APPS) }
            )

            // Privacy Profiles Card
            DashboardNavRow(
                icon = Icons.Default.Shield,
                title = "Privacy Profiles",
                subtitle = "Everyday, Public, Maximum Privacy",
                badgeText = state.activeProfile.name,
                isSuccess = true,
                tag = "nav_profiles",
                onClick = { onNavigate(NovaScreen.PROFILES) }
            )

            // Authentication & Lock
            DashboardNavRow(
                icon = Icons.Default.Lock,
                title = "Authentication & Lock",
                subtitle = if (state.isPinConfigured) "PIN configured • Keystore AES" else "Setup security PIN",
                badgeText = if (state.isPinConfigured) "ENCRYPTED" else "SETUP NEEDED",
                isSuccess = state.isPinConfigured,
                tag = "nav_auth",
                onClick = { onNavigate(NovaScreen.AUTH_SETTINGS) }
            )

            // Notification Privacy
            DashboardNavRow(
                icon = Icons.Default.Notifications,
                title = "Notification Privacy",
                subtitle = if (state.profileConfig.hideSensitiveNotifications) "Preview masking active" else "Standard preview visibility",
                badgeText = if (state.profileConfig.hideSensitiveNotifications) "MASKED" else "VISIBLE",
                isSuccess = state.profileConfig.hideSensitiveNotifications,
                tag = "nav_notifications",
                onClick = { onNavigate(NovaScreen.NOTIFICATION_SETTINGS) }
            )

            // Hardware Compatibility
            DashboardNavRow(
                icon = Icons.Default.DisplaySettings,
                title = "Hardware Compatibility",
                subtitle = "Viewing angle analysis & panel verification",
                badgeText = if (state.hardwareReport?.hasHardwarePrivacyDisplay == true) "SUPPORTED" else "UNSUPPORTED",
                isSuccess = state.hardwareReport?.hasHardwarePrivacyDisplay == true,
                tag = "nav_hardware",
                onClick = { onNavigate(NovaScreen.HARDWARE_COMPAT) }
            )

            // Privacy Activity History
            DashboardNavRow(
                icon = Icons.Default.History,
                title = "Privacy Activity History",
                subtitle = "${recentEvents.size} audit events recorded",
                badgeText = "AUDIT",
                isSuccess = true,
                tag = "nav_history",
                onClick = { onNavigate(NovaScreen.HISTORY) }
            )

            // About & Technical Disclosure
            DashboardNavRow(
                icon = Icons.Default.Info,
                title = "About & Technical Disclosure",
                subtitle = "Physics explanation & privacy policy",
                badgeText = "HONEST TECH",
                isSuccess = true,
                tag = "nav_about",
                onClick = { onNavigate(NovaScreen.ABOUT) }
            )
        }

        // Recent Activity Snapshot
        if (recentEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "RECENT PRIVACY AUDIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NovaTextSecondary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentEvents.take(3).forEach { event ->
                    NovaCard(borderColor = NovaDarkCardBorder) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
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
                                Text(
                                    text = event.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NovaTextPrimary
                                )
                                Text(
                                    text = event.description,
                                    fontSize = 11.sp,
                                    color = NovaTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    isSuccess: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    NovaCard(
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NovaElectricViolet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NovaCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = NovaTextSecondary
                )
            }
            NovaStatusBadge(
                text = badgeText,
                isSuccess = isSuccess,
                isWarning = !isSuccess
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = NovaTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
