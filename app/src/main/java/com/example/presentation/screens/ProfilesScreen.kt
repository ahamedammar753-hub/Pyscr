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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PrivacyProfile
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack

@Composable
fun ProfilesScreen(
    activeProfile: PrivacyProfile,
    onSelectProfile: (PrivacyProfile) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .verticalScroll(scrollState)
            .testTag("profiles_screen")
    ) {
        NovaTopBar(
            title = "Privacy Profiles",
            subtitle = "Switch protection postures instantly",
            onBack = onBack
        )

        Text(
            text = "AVAILABLE POSTURES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrivacyProfile.entries.forEach { profile ->
                val isSelected = (profile == activeProfile)
                val icon = when (profile) {
                    PrivacyProfile.EVERYDAY -> Icons.Default.WbSunny
                    PrivacyProfile.PUBLIC -> Icons.Default.Commute
                    PrivacyProfile.MAXIMUM_PRIVACY -> Icons.Default.Security
                    PrivacyProfile.CUSTOM -> Icons.Default.Tune
                }

                ProfileCard(
                    profile = profile,
                    isSelected = isSelected,
                    icon = icon,
                    onClick = { onSelectProfile(profile) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Rules Detail Explainer
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NovaCard(borderColor = NovaDarkCardBorder) {
                Column {
                    Text(
                        text = "PROFILE ENFORCEMENT DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaCyanNeon,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (activeProfile) {
                            PrivacyProfile.EVERYDAY -> "• Standard notification visibility\n• Essential app lock active (60s grace timeout)\n• Screen overlay deactivated to conserve battery"
                            PrivacyProfile.PUBLIC -> "• Sensitive notification content obscured\n• Active 70% opacity dimmer with Peephole slit enabled\n• Zero-second immediate app relocking"
                            PrivacyProfile.MAXIMUM_PRIVACY -> "• Maximum 85% opacity screen shield active\n• Biometric/PIN prompt on every application resume\n• FLAG_SECURE prevents Android recent app previews\n• All sensitive notification content masked"
                            PrivacyProfile.CUSTOM -> "• Custom rules controlled from Dashboard live controls and App Protection screens"
                        },
                        fontSize = 13.sp,
                        color = NovaTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileCard(
    profile: PrivacyProfile,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NovaCard(
        borderColor = if (isSelected) NovaCyanNeon else NovaDarkCardBorder,
        onClick = onClick,
        modifier = Modifier.testTag("profile_item_${profile.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) NovaCyanNeon.copy(alpha = 0.15f) else NovaDarkCardBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) NovaCyanNeon else NovaTextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        NovaStatusBadge(text = "CURRENT", isSuccess = true)
                    }
                }
                Text(
                    text = profile.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaElectricViolet
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.description,
                    fontSize = 11.sp,
                    color = NovaTextSecondary,
                    lineHeight = 16.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NovaCyanNeon,
                    unselectedColor = NovaTextSecondary
                ),
                modifier = Modifier.testTag("radio_${profile.name}")
            )
        }
    }
}
