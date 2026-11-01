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
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HardwareReport
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
fun HardwareCompatScreen(
    report: HardwareReport?,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isSupported = report?.hasHardwarePrivacyDisplay == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("hardware_compat_screen")
    ) {
        NovaTopBar(
            title = "Hardware Compatibility",
            subtitle = "Viewing angle hardware diagnostics",
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("btn_refresh_hardware")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = NovaCyanNeon
                    )
                }
            }
        )

        // Compatibility Verdict Card
        Box(modifier = Modifier.padding(16.dp)) {
            NovaCard(borderColor = if (isSupported) NovaSuccessGreen else NovaWarningAmber) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSupported) NovaSuccessGreen.copy(alpha = 0.2f) else NovaWarningAmber.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSupported) Icons.Default.DisplaySettings else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSupported) NovaSuccessGreen else NovaWarningAmber,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSupported) "Hardware Privacy Display Active" else "Standard Display Panel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = report?.honestySummary ?: "Analyzing device display subsystem...",
                            fontSize = 12.sp,
                            color = NovaTextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    NovaStatusBadge(
                        text = if (isSupported) "COMPATIBLE" else "UNSUPPORTED",
                        isSuccess = isSupported,
                        isWarning = !isSupported
                    )
                }
            }
        }

        // Diagnostic Telemetry Specs
        Text(
            text = "HARDWARE TELEMETRY & SPECIFICATIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryRow(label = "Manufacturer", value = report?.manufacturer ?: "Unknown")
                    TelemetryRow(label = "Device Model", value = report?.deviceModel ?: "Unknown")
                    TelemetryRow(label = "OS Platform", value = report?.androidVersion ?: "Unknown")
                    TelemetryRow(label = "Panel Technology", value = report?.panelTechnology ?: "OLED")
                    TelemetryRow(label = "Refresh Rate", value = "${report?.refreshRateHz?.toInt() ?: 60} Hz")
                    TelemetryRow(label = "Display Geometry", value = report?.displayMetricsInfo ?: "1080x2400")
                    TelemetryRow(label = "Detected OEM API", value = report?.detectedApiName ?: "None (Public API absent)")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Technical Honesty Disclosure Card
        Text(
            text = "PHYSICAL DISPLAY PHYSICS DISCLOSURE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaCyanNeon,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard(borderColor = NovaDarkCardBorder) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NovaCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HONEST PRIVACY ENGINEERING",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = report?.technicalExplanation ?: "",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = NovaTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
    }
}
