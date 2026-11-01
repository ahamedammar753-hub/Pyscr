package com.example.domain.model

enum class PrivacyStatus(val label: String) {
    PROTECTED("Fully Protected"),
    PARTIALLY_PROTECTED("Partially Protected"),
    ATTENTION_NEEDED("Permission Required"),
    INACTIVE("Protection Inactive")
}

data class HardwareReport(
    val hasHardwarePrivacyDisplay: Boolean,
    val manufacturer: String,
    val deviceModel: String,
    val androidVersion: String,
    val panelTechnology: String,
    val refreshRateHz: Float,
    val displayMetricsInfo: String,
    val privacyFeatureSupported: Boolean,
    val detectedApiName: String,
    val intensityLevelsSupported: List<String>,
    val honestySummary: String,
    val technicalExplanation: String
)

data class DiagnosticCheck(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPassed: Boolean,
    val isRequired: Boolean,
    val actionType: DiagnosticAction
)

enum class DiagnosticAction {
    SYSTEM_ALERT_WINDOW,
    USAGE_ACCESS,
    NOTIFICATION_LISTENER,
    BIOMETRIC_SETUP,
    KEYSTORE_VERIFIED,
    HARDWARE_DISPLAY_CHECK
}
