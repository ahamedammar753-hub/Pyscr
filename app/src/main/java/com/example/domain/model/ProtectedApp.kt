package com.example.domain.model

enum class AppCategory(val displayName: String) {
    MESSAGING("Messaging & Chat"),
    GALLERY("Photos & Gallery"),
    BANKING("Finance & Banking"),
    SOCIAL("Social Media"),
    EMAIL("Email & Work"),
    BROWSER("Web Browsers"),
    SYSTEM("System & Utilities"),
    OTHER("Other Applications")
}

data class ProtectedApp(
    val packageName: String,
    val appName: String,
    val isProtected: Boolean,
    val category: AppCategory,
    val customShieldOpacity: Float = 0.65f,
    val requireBiometrics: Boolean = true,
    val hideNotifications: Boolean = true,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)
