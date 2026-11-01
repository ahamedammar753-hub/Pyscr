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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaTopBar
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDangerRed
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack

@Composable
fun AboutScreen(
    onResetAllData: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("about_screen")
    ) {
        NovaTopBar(
            title = "About & Disclosure",
            subtitle = "Architecture, honesty & privacy policy",
            onBack = onBack
        )

        // Hero Brand Banner
        Box(modifier = Modifier.padding(16.dp)) {
            NovaCard(borderColor = NovaCyanNeon.copy(alpha = 0.5f)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NovaElectricViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = NovaCyanNeon,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "NOVA SCREEN GUARD",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "Your Screen. Your Privacy. Your Control.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = NovaCyanNeon
                            )
                            Text(
                                text = "Version 1.0.0 (Production Core)",
                                fontSize = 11.sp,
                                color = NovaTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Privacy By Design Guarantee
        Text(
            text = "PRIVACY-BY-DESIGN GUARANTEES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GuaranteeItem(title = "Local-First Vault", desc = "All rules, PIN hashes, and event logs remain strictly on this device in SQLite/Room.")
                    GuaranteeItem(title = "Zero Telemetry & Zero Ads", desc = "No external network tracking, no SDKs sending personal data to any server.")
                    GuaranteeItem(title = "Android Keystore Backed", desc = "PIN and sensitive secrets protected with hardware-isolated AES-GCM and PBKDF2 salt.")
                    GuaranteeItem(title = "Honest Engineering", desc = "We never claim software overlays physically restrict standard OLED viewing angles.")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Technical FAQ
        Text(
            text = "FREQUENTLY ASKED QUESTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard(borderColor = NovaDarkCardBorder) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FaqItem(
                        q = "Can a software overlay make my screen black from the side?",
                        a = "No ordinary Android application can physically block sideways photons on standard isotropic displays. Software overlays work by darkening the screen and reducing contrast so nearby observers cannot easily decipher text."
                    )
                    FaqItem(
                        q = "What is Peephole Focus Slit mode?",
                        a = "Peephole mode covers the top and bottom of your screen in heavy opacity while keeping a comfortable viewing aperture clear for you, minimizing exposure in crowded spaces."
                    )
                    FaqItem(
                        q = "Does Nova Screen Guard require root or private APIs?",
                        a = "No. Nova uses public Android APIs: SYSTEM_ALERT_WINDOW for overlays, BiometricPrompt, KeyStore, and NotificationListenerService."
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Data
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaButton(
                text = "Reset All Data & Revert to Defaults",
                onClick = { showResetDialog = true },
                isSecondary = true,
                modifier = Modifier.fillMaxWidth(),
                tag = "btn_reset_all_data"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Application Data?", color = NovaTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will wipe all configuration settings, PIN security, and audit history. Application will return to initial state.", color = NovaTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAllData()
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("dialog_confirm_reset_data")
                ) {
                    Text("Confirm Reset", color = NovaDangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            },
            containerColor = NovaVoidBlack
        )
    }
}

@Composable
private fun GuaranteeItem(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NovaCyanNeon)
        Text(text = desc, fontSize = 11.sp, color = NovaTextSecondary, lineHeight = 16.sp)
    }
}

@Composable
private fun FaqItem(q: String, a: String) {
    Column {
        Text(text = q, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = a, fontSize = 11.sp, color = NovaTextSecondary, lineHeight = 16.sp)
    }
}
