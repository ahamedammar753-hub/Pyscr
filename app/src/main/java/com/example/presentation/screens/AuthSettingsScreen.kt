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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.presentation.components.PinLockDialog
import com.example.presentation.components.PinMode
import com.example.presentation.components.SettingToggleRow
import com.example.presentation.viewmodel.NovaUiState
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCard
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack

@Composable
fun AuthSettingsScreen(
    state: NovaUiState,
    onSavePin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onSetLockTimeout: (Int) -> Unit,
    onTestBiometric: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showPinDialog by remember { mutableStateOf(false) }
    var pinMode by remember { mutableStateOf(PinMode.SETUP) }
    var testAuthSuccessMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("auth_settings_screen")
    ) {
        NovaTopBar(
            title = "Authentication & Lock",
            subtitle = "Android Keystore-backed PIN & Biometrics",
            onBack = onBack
        )

        // Status Card
        Box(modifier = Modifier.padding(16.dp)) {
            NovaCard(borderColor = if (state.isPinConfigured) NovaCyanNeon else NovaDarkCardBorder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NovaElectricViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = NovaCyanNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isPinConfigured) "Security PIN Configured" else "PIN Not Configured",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = if (state.isPinConfigured) "PBKDF2 salted hash with Keystore AES" else "Create a 4-digit PIN for application-level lock",
                            fontSize = 12.sp,
                            color = NovaTextSecondary
                        )
                    }

                    NovaStatusBadge(
                        text = if (state.isPinConfigured) "ENCRYPTED" else "UNSET",
                        isSuccess = state.isPinConfigured,
                        isWarning = !state.isPinConfigured
                    )
                }
            }
        }

        // PIN Actions
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaButton(
                text = if (state.isPinConfigured) "Change Security PIN" else "Establish Security PIN",
                onClick = {
                    pinMode = PinMode.SETUP
                    showPinDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                tag = "btn_setup_pin"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Biometric Settings Section
        Text(
            text = "BIOMETRIC AUTHENTICATION",
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
                        title = "Biometric Unlock (Fingerprint / Face)",
                        description = "Use device biometric sensor to bypass PIN entry",
                        isChecked = state.isBiometricEnabled,
                        onCheckedChange = onToggleBiometric,
                        icon = Icons.Default.Fingerprint,
                        tag = "toggle_biometric"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NovaButton(
                        text = "Test Biometric Authentication",
                        onClick = onTestBiometric,
                        isSecondary = true,
                        modifier = Modifier.fillMaxWidth(),
                        tag = "btn_test_biometric"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Relock Timeout Section
        Text(
            text = "AUTOMATIC RELOCK TIMEOUT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        val timeoutOptions = listOf(
            0 to "Immediately on app exit",
            30 to "30 Seconds grace",
            60 to "1 Minute grace",
            300 to "5 Minutes grace"
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    timeoutOptions.forEach { (seconds, label) ->
                        val isSelected = (state.lockTimeoutSeconds == seconds)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NovaDarkCardBorder else NovaDarkCard)
                                .clickable { onSetLockTimeout(seconds) }
                                .padding(12.dp)
                                .testTag("timeout_$seconds"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isSelected) NovaCyanNeon else NovaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NovaCyanNeon else NovaTextPrimary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = NovaCyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recovery Phrase Card
        if (state.recoveryPhrase.isNotBlank() && state.recoveryPhrase != "Not generated") {
            Text(
                text = "RECOVERY PHRASE (STORE OFFLINE)",
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
                            text = state.recoveryPhrase,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaCyanNeon,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "If you forget your security PIN, this phrase is required to reset it without losing your protected app configuration.",
                            fontSize = 11.sp,
                            color = NovaTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showPinDialog) {
        PinLockDialog(
            mode = pinMode,
            onSuccess = {
                showPinDialog = false
            },
            onDismiss = {
                showPinDialog = false
            },
            onSaveNewPin = { newPin ->
                onSavePin(newPin)
                showPinDialog = false
            },
            verifyPinHash = onVerifyPin,
            recoveryPhraseExpected = state.recoveryPhrase
        )
    }
}
