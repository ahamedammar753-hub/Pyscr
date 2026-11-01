package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCard
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaSuccessGreen
import com.example.ui.theme.NovaWarningAmber

@Composable
fun NovaShieldBadge(
    score: Int,
    isProtected: Boolean,
    modifier: Modifier = Modifier,
    statusText: String = if (isProtected) "Active Shield" else "Partial Shield"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .testTag("nova_shield_badge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(175.dp)) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Outer pulsing glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NovaElectricViolet.copy(alpha = glowAlpha),
                        NovaCyanNeon.copy(alpha = glowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = width * 0.52f * pulseScale
                ),
                radius = width * 0.52f * pulseScale,
                center = Offset(centerX, centerY)
            )

            // High-tech Shield Path
            val shieldPath = Path().apply {
                moveTo(centerX, height * 0.08f)
                // Top right crest
                cubicTo(
                    width * 0.82f, height * 0.08f,
                    width * 0.90f, height * 0.22f,
                    width * 0.90f, height * 0.45f
                )
                // Bottom point taper
                cubicTo(
                    width * 0.90f, height * 0.72f,
                    centerX, height * 0.92f,
                    centerX, height * 0.96f
                )
                // Bottom left taper
                cubicTo(
                    centerX, height * 0.92f,
                    width * 0.10f, height * 0.72f,
                    width * 0.10f, height * 0.45f
                )
                // Top left crest
                cubicTo(
                    width * 0.10f, height * 0.22f,
                    width * 0.18f, height * 0.08f,
                    centerX, height * 0.08f
                )
                close()
            }

            // Shield fill
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NovaDarkCard,
                        Color(0xFF0C101A)
                    )
                )
            )

            // Shield neon border stroke
            drawPath(
                path = shieldPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NovaCyanNeon,
                        NovaElectricViolet,
                        NovaCyanNeon
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(width, height)
                ),
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner cybernetic concentric line
            drawCircle(
                color = NovaElectricViolet.copy(alpha = 0.35f),
                radius = width * 0.28f,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score%",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (score >= 80) NovaCyanNeon else NovaWarningAmber,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusText.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NovaElectricViolet,
                letterSpacing = 1.5.sp
            )
        }
    }
}
