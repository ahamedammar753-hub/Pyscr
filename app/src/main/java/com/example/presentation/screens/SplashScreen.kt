package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaShieldBadge
import com.example.presentation.navigation.NovaScreen
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isOnboardingDone: Boolean,
    onNavigate: (NovaScreen) -> Unit
) {
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        isReady = true
        delay(800)
        if (isOnboardingDone) {
            onNavigate(NovaScreen.DASHBOARD)
        } else {
            onNavigate(NovaScreen.ONBOARDING)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .clickable {
                if (isOnboardingDone) {
                    onNavigate(NovaScreen.DASHBOARD)
                } else {
                    onNavigate(NovaScreen.ONBOARDING)
                }
            }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            NovaShieldBadge(
                score = 100,
                isProtected = true,
                statusText = "Nova Guard"
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "NOVA SCREEN GUARD",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = NovaTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Screen. Your Privacy. Your Control.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NovaCyanNeon,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            AnimatedVisibility(visible = isReady, enter = fadeIn()) {
                Text(
                    text = "INITIALIZING SECURE VAULT...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = NovaElectricViolet
                )
            }
        }
    }
}
