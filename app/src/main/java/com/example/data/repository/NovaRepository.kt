package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.db.AppProtectionEntity
import com.example.data.db.NovaDatabase
import com.example.data.db.PrivacyEventEntity
import com.example.domain.model.AppCategory
import com.example.domain.model.OverlayTint
import com.example.domain.model.PrivacyProfile
import com.example.domain.model.ProfileConfig
import com.example.domain.model.ProtectedApp
import com.example.security.AuthPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NovaRepository(
    private val context: Context,
    private val database: NovaDatabase,
    private val authPreferences: AuthPreferences
) {
    private val dao = database.novaDao()

    val protectedAppsFlow: Flow<List<ProtectedApp>> = dao.getAllProtectedApps().map { entities ->
        entities.map { entity ->
            ProtectedApp(
                packageName = entity.packageName,
                appName = entity.appName,
                isProtected = entity.isProtected,
                category = parseCategory(entity.category),
                customShieldOpacity = entity.customShieldOpacity,
                requireBiometrics = entity.requireBiometrics,
                hideNotifications = entity.hideNotifications,
                lastModifiedTimestamp = entity.lastModifiedTimestamp
            )
        }
    }

    val activeProtectedAppsFlow: Flow<List<ProtectedApp>> = dao.getActiveProtectedApps().map { entities ->
        entities.map { entity ->
            ProtectedApp(
                packageName = entity.packageName,
                appName = entity.appName,
                isProtected = entity.isProtected,
                category = parseCategory(entity.category),
                customShieldOpacity = entity.customShieldOpacity,
                requireBiometrics = entity.requireBiometrics,
                hideNotifications = entity.hideNotifications,
                lastModifiedTimestamp = entity.lastModifiedTimestamp
            )
        }
    }

    val privacyEventsFlow: Flow<List<PrivacyEventEntity>> = dao.getAllEvents()

    suspend fun syncInstalledApplications() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)

        val existingApps = dao.getAllProtectedApps()
        // We scan all installed launchable apps
        val entities = mutableListOf<AppProtectionEntity>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            // Skip Nova Screen Guard itself
            if (pkg == context.packageName) continue

            val appName = resolveInfo.loadLabel(pm).toString()
            val category = detectAppCategory(pkg, appName)
            val existing = dao.getApp(pkg)

            if (existing != null) {
                // Keep user's existing protection preference
                entities.add(existing.copy(appName = appName))
            } else {
                // Default high-privacy sensitive apps (banking, messaging, gallery) to protected
                val isSensitiveByDefault = category == AppCategory.BANKING ||
                        category == AppCategory.MESSAGING ||
                        category == AppCategory.GALLERY

                entities.add(
                    AppProtectionEntity(
                        packageName = pkg,
                        appName = appName,
                        isProtected = isSensitiveByDefault,
                        category = category.name,
                        customShieldOpacity = 0.65f,
                        requireBiometrics = true,
                        hideNotifications = true
                    )
                )
            }
        }

        // Add essential sample entries if emulator has few launchable apps
        if (entities.size < 4) {
            val sampleApps = listOf(
                AppProtectionEntity("com.android.gallery3d", "Private Gallery", true, AppCategory.GALLERY.name),
                AppProtectionEntity("com.whatsapp.vault", "Secure Messenger", true, AppCategory.MESSAGING.name),
                AppProtectionEntity("com.bank.finance", "Mobile Banking Pro", true, AppCategory.BANKING.name),
                AppProtectionEntity("com.google.android.gm", "Confidential Mail", false, AppCategory.EMAIL.name),
                AppProtectionEntity("com.android.chrome", "Incognito Browser", false, AppCategory.BROWSER.name)
            )
            for (sample in sampleApps) {
                if (entities.none { it.packageName == sample.packageName }) {
                    entities.add(sample)
                }
            }
        }

        dao.insertApps(entities)
    }

    suspend fun toggleAppProtection(packageName: String, isProtected: Boolean) = withContext(Dispatchers.IO) {
        dao.setAppProtection(packageName, isProtected)
        val app = dao.getApp(packageName)
        val appName = app?.appName ?: packageName
        val action = if (isProtected) "Shield Activated" else "Shield Deactivated"
        val severity = if (isProtected) "SUCCESS" else "INFO"

        dao.insertEvent(
            PrivacyEventEntity(
                eventType = "APP_PROTECTION_TOGGLED",
                title = "$action: $appName",
                description = "Protection status changed to ${if (isProtected) "Active" else "Disabled"}",
                severity = severity
            )
        )
    }

    suspend fun updateAppDetails(app: ProtectedApp) = withContext(Dispatchers.IO) {
        dao.updateApp(
            AppProtectionEntity(
                packageName = app.packageName,
                appName = app.appName,
                isProtected = app.isProtected,
                category = app.category.name,
                customShieldOpacity = app.customShieldOpacity,
                requireBiometrics = app.requireBiometrics,
                hideNotifications = app.hideNotifications,
                lastModifiedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun setAllAppsProtection(isProtected: Boolean) = withContext(Dispatchers.IO) {
        dao.setAllProtection(isProtected)
        dao.insertEvent(
            PrivacyEventEntity(
                eventType = "BATCH_PROTECTION_UPDATE",
                title = if (isProtected) "All Applications Protected" else "All Protections Cleared",
                description = "Bulk status update applied across all installed applications",
                severity = if (isProtected) "SUCCESS" else "WARNING"
            )
        )
    }

    suspend fun resetProtectionToDefaults() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            val appName = info.loadLabel(pm).toString()
            val cat = detectAppCategory(pkg, appName)
            val shouldProtect = cat == AppCategory.BANKING || cat == AppCategory.MESSAGING || cat == AppCategory.GALLERY
            dao.setAppProtection(pkg, shouldProtect)
        }
        dao.insertEvent(
            PrivacyEventEntity(
                eventType = "PROTECTION_RESET",
                title = "Protection Reset to Defaults",
                description = "Sensitive categories (Banking, Messaging, Gallery) protected by default",
                severity = "INFO"
            )
        )
    }

    suspend fun recordEvent(eventType: String, title: String, description: String, severity: String = "INFO") = withContext(Dispatchers.IO) {
        dao.insertEvent(
            PrivacyEventEntity(
                eventType = eventType,
                title = title,
                description = description,
                severity = severity
            )
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAllEvents()
    }

    fun getProfileConfig(profile: PrivacyProfile): ProfileConfig {
        return when (profile) {
            PrivacyProfile.EVERYDAY -> ProfileConfig(
                profile = PrivacyProfile.EVERYDAY,
                overlayEnabled = false,
                overlayOpacity = 0.50f,
                overlayTint = OverlayTint.PITCH_BLACK,
                peepholeModeEnabled = false,
                appLockEnabled = true,
                requireBiometrics = true,
                lockTimeoutSeconds = 60,
                hideSensitiveNotifications = false,
                flagSecureEnabled = true
            )
            PrivacyProfile.PUBLIC -> ProfileConfig(
                profile = PrivacyProfile.PUBLIC,
                overlayEnabled = true,
                overlayOpacity = 0.70f,
                overlayTint = OverlayTint.CHARCOAL_STEALTH,
                peepholeModeEnabled = true,
                appLockEnabled = true,
                requireBiometrics = true,
                lockTimeoutSeconds = 0, // Immediate
                hideSensitiveNotifications = true,
                flagSecureEnabled = true
            )
            PrivacyProfile.MAXIMUM_PRIVACY -> ProfileConfig(
                profile = PrivacyProfile.MAXIMUM_PRIVACY,
                overlayEnabled = true,
                overlayOpacity = 0.85f,
                overlayTint = OverlayTint.PITCH_BLACK,
                peepholeModeEnabled = false,
                appLockEnabled = true,
                requireBiometrics = true,
                lockTimeoutSeconds = 0,
                hideSensitiveNotifications = true,
                flagSecureEnabled = true
            )
            PrivacyProfile.CUSTOM -> ProfileConfig(
                profile = PrivacyProfile.CUSTOM,
                overlayEnabled = authPreferences.isOverlayActive,
                overlayOpacity = authPreferences.overlayOpacity,
                overlayTint = OverlayTint.valueOf(authPreferences.overlayTintName),
                peepholeModeEnabled = authPreferences.isPeepholeActive,
                appLockEnabled = authPreferences.isAppLockGlobalEnabled,
                requireBiometrics = authPreferences.isBiometricEnabled,
                lockTimeoutSeconds = authPreferences.lockTimeoutSeconds,
                hideSensitiveNotifications = authPreferences.hideSensitiveNotifications,
                flagSecureEnabled = authPreferences.flagSecureEnabled
            )
        }
    }

    private fun detectAppCategory(pkg: String, name: String): AppCategory {
        val lower = (pkg + " " + name).lowercase()
        return when {
            lower.contains("bank") || lower.contains("pay") || lower.contains("wallet") ||
                    lower.contains("chase") || lower.contains("finance") || lower.contains("cash") ||
                    lower.contains("crypto") || lower.contains("revolut") -> AppCategory.BANKING

            lower.contains("message") || lower.contains("chat") || lower.contains("whatsapp") ||
                    lower.contains("telegram") || lower.contains("signal") || lower.contains("slack") ||
                    lower.contains("discord") || lower.contains("viber") || lower.contains("sms") -> AppCategory.MESSAGING

            lower.contains("gallery") || lower.contains("photo") || lower.contains("camera") ||
                    lower.contains("image") -> AppCategory.GALLERY

            lower.contains("social") || lower.contains("insta") || lower.contains("tiktok") ||
                    lower.contains("tweet") || lower.contains("twitter") || lower.contains("face") ||
                    lower.contains("reddit") || lower.contains("snap") || lower.contains("linked") -> AppCategory.SOCIAL

            lower.contains("mail") || lower.contains("outlook") || lower.contains("gmail") ||
                    lower.contains("inbox") -> AppCategory.EMAIL

            lower.contains("browser") || lower.contains("chrome") || lower.contains("firefox") ||
                    lower.contains("opera") || lower.contains("edge") || lower.contains("brave") -> AppCategory.BROWSER

            lower.contains("setting") || lower.contains("system") || lower.contains("android") ||
                    lower.contains("tool") -> AppCategory.SYSTEM

            else -> AppCategory.OTHER
        }
    }

    private fun parseCategory(name: String): AppCategory {
        return try {
            AppCategory.valueOf(name)
        } catch (e: Exception) {
            AppCategory.OTHER
        }
    }
}
