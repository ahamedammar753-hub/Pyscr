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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.domain.model.ProtectedApp
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.presentation.components.SettingToggleRow
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppDetailScreen(
    app: ProtectedApp,
    onSave: (ProtectedApp) -> Unit,
    onBack: () -> Unit
) {
    var isProtected by remember(app) { mutableStateOf(app.isProtected) }
    var shieldOpacity by remember(app) { mutableFloatStateOf(app.customShieldOpacity) }
    var requireBiometrics by remember(app) { mutableStateOf(app.requireBiometrics) }
    var hideNotifications by remember(app) { mutableStateOf(app.hideNotifications) }

    val scrollState = rememberScrollState()

    val formattedDate = remember(app.lastModifiedTimestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(app.lastModifiedTimestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("app_detail_screen")
    ) {
        NovaTopBar(
            title = "App Protection Settings",
            subtitle = app.appName,
            onBack = onBack
        )

        // App Header Banner
        Box(modifier = Modifier.padding(16.dp)) {
            NovaCard(borderColor = if (isProtected) NovaCyanNeon else NovaDarkCardBorder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaElectricViolet.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.firstOrNull()?.uppercase() ?: "A",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NovaCyanNeon
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = app.category.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NovaElectricViolet
                        )
                        Text(
                            text = app.packageName,
                            fontSize = 11.sp,
                            color = NovaTextMuted
                        )
                    }

                    NovaStatusBadge(
                        text = if (isProtected) "PROTECTED" else "UNGUARDED",
                        isSuccess = isProtected,
                        isWarning = !isProtected
                    )
                }
            }
        }

        // Configuration Section
        Text(
            text = "CUSTOM PROTECTION RULES",
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
                        title = "Enable Guard Shield",
                        description = "Activate stealth privacy enforcement when opening ${app.appName}",
                        isChecked = isProtected,
                        onCheckedChange = { isProtected = it },
                        icon = Icons.Default.Security,
                        tag = "app_detail_shield_toggle"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Require Biometric / PIN Lock",
                        description = "Prompt for authentication before allowing vault access",
                        isChecked = requireBiometrics,
                        onCheckedChange = { requireBiometrics = it },
                        enabled = isProtected,
                        icon = Icons.Default.Fingerprint,
                        tag = "app_detail_biometric_toggle"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Mask Notification Previews",
                        description = "Obscure incoming alerts from this app on lock screen and banner",
                        isChecked = hideNotifications,
                        onCheckedChange = { hideNotifications = it },
                        enabled = isProtected,
                        icon = Icons.Default.NotificationsOff,
                        tag = "app_detail_notif_toggle"
                    )

                    if (isProtected) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "App Screen Shield Opacity",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "${(shieldOpacity * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaCyanNeon
                            )
                        }

                        Slider(
                            value = shieldOpacity,
                            onValueChange = { shieldOpacity = it },
                            valueRange = 0.20f..0.90f,
                            colors = SliderDefaults.colors(
                                thumbColor = NovaCyanNeon,
                                activeTrackColor = NovaCyanNeon,
                                inactiveTrackColor = NovaDarkCardBorder
                            ),
                            modifier = Modifier.testTag("app_detail_opacity_slider")
                        )
                    }
                }
            }
        }

        // Metadata Card
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard(borderColor = NovaDarkCardBorder) {
                Column {
                    Text(
                        text = "LAST CONFIGURATION UPDATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 13.sp,
                        color = NovaTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaButton(
                text = "Save App Protection Policy",
                onClick = {
                    val updated = app.copy(
                        isProtected = isProtected,
                        customShieldOpacity = shieldOpacity,
                        requireBiometrics = requireBiometrics,
                        hideNotifications = hideNotifications,
                        lastModifiedTimestamp = System.currentTimeMillis()
                    )
                    onSave(updated)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                tag = "app_detail_save_btn"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
