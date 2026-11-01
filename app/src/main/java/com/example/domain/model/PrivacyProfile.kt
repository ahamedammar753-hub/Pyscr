package com.example.domain.model

enum class PrivacyProfile(
    val title: String,
    val subtitle: String,
    val description: String
) {
    EVERYDAY(
        title = "Everyday Mode",
        subtitle = "Balanced Daily Privacy",
        description = "Standard notification visibility with essential app-level locks and no disruptive screen overlays."
    ),
    PUBLIC(
        title = "Public Mode",
        subtitle = "Transit & Crowded Spaces",
        description = "Obscures sensitive notification contents, activates the stealth screen shield with peephole, and enforces immediate app relock."
    ),
    MAXIMUM_PRIVACY(
        title = "Maximum Privacy Mode",
        subtitle = "Zero-Exposure Lockdown",
        description = "Enforces high-opacity screen guard, FLAG_SECURE on all sensitive windows, biometric authentication on every resume, and strict notification preview masking."
    ),
    CUSTOM(
        title = "Custom Mode",
        subtitle = "User Configured",
        description = "Fine-tuned privacy rules, adjustable dimmer opacity, and custom protection policies."
    )
}

data class ProfileConfig(
    val profile: PrivacyProfile = PrivacyProfile.EVERYDAY,
    val overlayEnabled: Boolean = false,
    val overlayOpacity: Float = 0.60f,
    val overlayTint: OverlayTint = OverlayTint.PITCH_BLACK,
    val peepholeModeEnabled: Boolean = false,
    val peepholeHeightFraction: Float = 0.22f,
    val appLockEnabled: Boolean = true,
    val requireBiometrics: Boolean = true,
    val lockTimeoutSeconds: Int = 0, // 0 = Immediately
    val hideSensitiveNotifications: Boolean = false,
    val flagSecureEnabled: Boolean = true
)

enum class OverlayTint(val label: String, val argb: Long) {
    PITCH_BLACK("Pitch Black", 0xFF000000),
    CHARCOAL_STEALTH("Charcoal Stealth", 0xFF0A0F1D),
    CYBER_VIOLET("Cyber Violet", 0xFF1E0A3C),
    AMBER_FILTER("Night Amber", 0xFF2A1B05)
}
