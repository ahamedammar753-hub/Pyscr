package com.example.security

import android.content.Context
import android.content.SharedPreferences

class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "nova_security_prefs",
        Context.MODE_PRIVATE
    )

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var isPinConfigured: Boolean
        get() = prefs.getBoolean(KEY_PIN_CONFIGURED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_CONFIGURED, value).apply()

    var pinSalt: String?
        get() = prefs.getString(KEY_PIN_SALT, null)
        set(value) = prefs.edit().putString(KEY_PIN_SALT, value).apply()

    var pinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    var recoveryPhraseHash: String?
        get() = prefs.getString(KEY_RECOVERY_PHRASE, null)
        set(value) = prefs.edit().putString(KEY_RECOVERY_PHRASE, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isAppLockGlobalEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_GLOBAL, true)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_GLOBAL, value).apply()

    var lockTimeoutSeconds: Int
        get() = prefs.getInt(KEY_LOCK_TIMEOUT_SECONDS, 0) // 0 = Immediately
        set(value) = prefs.edit().putInt(KEY_LOCK_TIMEOUT_SECONDS, value).apply()

    var activeProfileName: String
        get() = prefs.getString(KEY_ACTIVE_PROFILE, "EVERYDAY") ?: "EVERYDAY"
        set(value) = prefs.edit().putString(KEY_ACTIVE_PROFILE, value).apply()

    var isOverlayActive: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ACTIVE, value).apply()

    var overlayOpacity: Float
        get() = prefs.getFloat(KEY_OVERLAY_OPACITY, 0.60f)
        set(value) = prefs.edit().putFloat(KEY_OVERLAY_OPACITY, value).apply()

    var overlayTintName: String
        get() = prefs.getString(KEY_OVERLAY_TINT, "PITCH_BLACK") ?: "PITCH_BLACK"
        set(value) = prefs.edit().putString(KEY_OVERLAY_TINT, value).apply()

    var isPeepholeActive: Boolean
        get() = prefs.getBoolean(KEY_PEEPHOLE_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_PEEPHOLE_ACTIVE, value).apply()

    var hideSensitiveNotifications: Boolean
        get() = prefs.getBoolean(KEY_HIDE_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_HIDE_NOTIFICATIONS, value).apply()

    var flagSecureEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLAG_SECURE, true)
        set(value) = prefs.edit().putBoolean(KEY_FLAG_SECURE, value).apply()

    var failedAttempts: Int
        get() = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        set(value) = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, value).apply()

    var lockoutUntilTimestamp: Long
        get() = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_LOCKOUT_UNTIL, value).apply()

    var lastUnlockedTimestamp: Long
        get() = prefs.getLong(KEY_LAST_UNLOCKED, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UNLOCKED, value).apply()

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_PIN_CONFIGURED = "pin_configured"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_RECOVERY_PHRASE = "recovery_phrase_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APP_LOCK_GLOBAL = "app_lock_global"
        private const val KEY_LOCK_TIMEOUT_SECONDS = "lock_timeout_seconds"
        private const val KEY_ACTIVE_PROFILE = "active_profile"
        private const val KEY_OVERLAY_ACTIVE = "overlay_active"
        private const val KEY_OVERLAY_OPACITY = "overlay_opacity"
        private const val KEY_OVERLAY_TINT = "overlay_tint"
        private const val KEY_PEEPHOLE_ACTIVE = "peephole_active"
        private const val KEY_HIDE_NOTIFICATIONS = "hide_notifications"
        private const val KEY_FLAG_SECURE = "flag_secure"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_LAST_UNLOCKED = "last_unlocked"
    }
}
