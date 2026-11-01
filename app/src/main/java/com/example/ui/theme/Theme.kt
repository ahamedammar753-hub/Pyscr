package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NovaElectricViolet,
    onPrimary = Color.Black,
    primaryContainer = NovaDarkCard,
    onPrimaryContainer = NovaCyanNeon,
    secondary = NovaCyanNeon,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003830),
    onSecondaryContainer = NovaCyanNeon,
    tertiary = NovaElectricBlue,
    onTertiary = Color.Black,
    background = NovaVoidBlack,
    onBackground = NovaTextPrimary,
    surface = NovaDarkSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaDarkCard,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaDarkCardBorder,
    error = NovaDangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NovaVioletGlow,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = NovaVioletGlow,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = NovaCyanGlow,
    onTertiary = Color.White,
    background = NovaLightBackground,
    onBackground = NovaLightTextPrimary,
    surface = NovaLightSurface,
    onSurface = NovaLightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = NovaLightTextSecondary,
    outline = NovaLightCardBorder,
    error = NovaDangerRed,
    onError = Color.White
)

@Composable
fun NovaGuardTheme(
    darkTheme: Boolean = true, // Dark mode is default as per prompt
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
