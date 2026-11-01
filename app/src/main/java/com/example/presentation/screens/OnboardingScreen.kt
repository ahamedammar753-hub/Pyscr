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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.NovaButton
import com.example.presentation.components.NovaCard
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val note: String
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Realistic Screen Privacy",
            subtitle = "Hardware Diagnostics & Software Stealth",
            description = "Physical privacy screens restrict light at the display hardware layer. On standard devices, Nova Screen Guard detects your real hardware capability and provides high-contrast software dimmers and peephole apertures to thwart casual shoulder-surfers.",
            icon = Icons.Default.VisibilityOff,
            note = "Transparent: Software overlays reduce contrast but do not alter physical viewing angles."
        ),
        OnboardingPageData(
            title = "App-Specific Protection",
            subtitle = "Isolate Sensitive Applications",
            description = "Shield banking, messaging, gallery, and work email apps individually. Customize privacy intensity and authentication requirements per application.",
            icon = Icons.Default.Apps,
            note = "Choose exactly which apps trigger screen privacy shields."
        ),
        OnboardingPageData(
            title = "Adaptive Privacy Profiles",
            subtitle = "Everyday • Public • Maximum Privacy",
            description = "Switch between balanced daily privacy and zero-exposure lockdowns when commuting or standing in crowded transit. Secure with Android KeyStore-backed PIN and Biometrics.",
            icon = Icons.Default.Security,
            note = "Zero tracking. Local-first. Your secrets never leave this device."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .padding(24.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top brand indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NovaCyanNeon)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NOVA SCREEN GUARD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = NovaCyanNeon
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(NovaElectricViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = NovaCyanNeon,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = page.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NovaTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaElectricViolet,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    fontSize = 13.sp,
                    color = NovaTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                NovaCard(
                    borderColor = NovaDarkCardBorder
                ) {
                    Text(
                        text = page.note,
                        fontSize = 11.sp,
                        color = NovaCyanNeon,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Pager Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { i ->
                val isSelected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NovaCyanNeon else NovaDarkCardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage > 0) {
                NovaButton(
                    text = "Back",
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    isSecondary = true,
                    modifier = Modifier.weight(1f),
                    tag = "onboarding_back"
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            NovaButton(
                text = if (pagerState.currentPage == pages.size - 1) "Enter Guard Vault" else "Continue",
                onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                tag = "onboarding_next"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
