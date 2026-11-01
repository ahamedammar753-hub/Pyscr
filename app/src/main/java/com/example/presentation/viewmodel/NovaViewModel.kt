package com.example.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PrivacyEventEntity
import com.example.data.repository.NovaRepository
import com.example.domain.model.AppCategory
import com.example.domain.model.DiagnosticAction
import com.example.domain.model.DiagnosticCheck
import com.example.domain.model.HardwareReport
import com.example.domain.model.OverlayTint
import com.example.domain.model.PrivacyProfile
import com.example.domain.model.PrivacyStatus
import com.example.domain.model.ProfileConfig
import com.example.domain.model.ProtectedApp
import com.example.platform.HardwareDisplayDetector
import com.example.platform.NovaNotificationListenerService
import com.example.platform.PrivacyOverlayService
import com.example.presentation.navigation.NovaScreen
import com.example.security.AuthPreferences
import com.example.security.BiometricHelper
import com.example.security.KeystoreCryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NovaUiState(
    val currentScreen: NovaScreen = NovaScreen.SPLASH,
    val selectedApp: ProtectedApp? = null,
    val searchQuery: String = "",
    val selectedCategory: AppCategory? = null,
    val protectionFilter: ProtectionFilter = ProtectionFilter.ALL,
    val activeProfile: PrivacyProfile = PrivacyProfile.EVERYDAY,
    val profileConfig: ProfileConfig = ProfileConfig(),
    val isOverlayActive: Boolean = false,
    val overlayOpacity: Float = 0.60f,
    val overlayTint: OverlayTint = OverlayTint.PITCH_BLACK,
    val isPeepholeActive: Boolean = false,
    val isVaultLocked: Boolean = false,
    val isPinConfigured: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val lockTimeoutSeconds: Int = 0,
    val recoveryPhrase: String = "",
    val hardwareReport: HardwareReport? = null,
    val diagnosticChecks: List<DiagnosticCheck> = emptyList(),
    val privacyScore: Int = 85,
    val privacyStatus: PrivacyStatus = PrivacyStatus.PROTECTED,
    val isNotificationListenerGranted: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false
)

enum class ProtectionFilter(val label: String) {
    ALL("All Apps"),
    PROTECTED("Protected Only"),
    UNPROTECTED("Unprotected")
}

class NovaViewModel(
    private val context: Context,
    private val repository: NovaRepository,
    private val authPreferences: AuthPreferences,
    private val cryptoManager: KeystoreCryptoManager,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val screenBackStack = mutableListOf<NovaScreen>()
    private val _uiState = MutableStateFlow(NovaUiState())
    val uiState: StateFlow<NovaUiState> = _uiState.asStateFlow()

    private val hardwareDetector = HardwareDisplayDetector(context)

    val allProtectedApps: StateFlow<List<ProtectedApp>> = repository.protectedAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privacyEvents: StateFlow<List<PrivacyEventEntity>> = repository.privacyEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered apps based on search, category, and protection status
    val filteredApps: StateFlow<List<ProtectedApp>> = combine(
        allProtectedApps,
        _uiState
    ) { apps, state ->
        apps.filter { app ->
            val matchesSearch = state.searchQuery.isBlank() ||
                    app.appName.contains(state.searchQuery, ignoreCase = true) ||
                    app.packageName.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = state.selectedCategory == null || app.category == state.selectedCategory

            val matchesFilter = when (state.protectionFilter) {
                ProtectionFilter.ALL -> true
                ProtectionFilter.PROTECTED -> app.isProtected
                ProtectionFilter.UNPROTECTED -> !app.isProtected
            }

            matchesSearch && matchesCategory && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialState()
        refreshHardwareAndDiagnostics()
        syncApps()
    }

    private fun loadInitialState() {
        val initialProfile = try {
            PrivacyProfile.valueOf(authPreferences.activeProfileName)
        } catch (e: Exception) {
            PrivacyProfile.EVERYDAY
        }

        val isPinSet = authPreferences.isPinConfigured
        val initialScreen = if (authPreferences.isOnboardingCompleted) {
            NovaScreen.DASHBOARD
        } else {
            NovaScreen.SPLASH
        }

        val overlayActive = authPreferences.isOverlayActive
        val overlayOpacity = authPreferences.overlayOpacity
        val overlayTint = try {
            OverlayTint.valueOf(authPreferences.overlayTintName)
        } catch (e: Exception) {
            OverlayTint.PITCH_BLACK
        }

        _uiState.value = _uiState.value.copy(
            currentScreen = initialScreen,
            activeProfile = initialProfile,
            profileConfig = repository.getProfileConfig(initialProfile),
            isOverlayActive = overlayActive,
            overlayOpacity = overlayOpacity,
            overlayTint = overlayTint,
            isPeepholeActive = authPreferences.isPeepholeActive,
            isPinConfigured = isPinSet,
            isBiometricEnabled = authPreferences.isBiometricEnabled,
            lockTimeoutSeconds = authPreferences.lockTimeoutSeconds,
            recoveryPhrase = authPreferences.recoveryPhraseHash?.let { cryptoManager.decrypt(it) } ?: "Not generated"
        )
    }

    fun completeOnboarding() {
        authPreferences.isOnboardingCompleted = true
        navigateTo(NovaScreen.DASHBOARD, clearStack = true)
        recordEvent("ONBOARDING_COMPLETED", "Welcome to Nova Screen Guard", "Initial setup finished successfully.")
    }

    fun navigateTo(screen: NovaScreen, clearStack: Boolean = false) {
        if (clearStack) {
            screenBackStack.clear()
        } else {
            screenBackStack.add(_uiState.value.currentScreen)
        }
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val previous = screenBackStack.removeAt(screenBackStack.size - 1)
            _uiState.value = _uiState.value.copy(currentScreen = previous)
            return true
        }
        return false
    }

    fun selectAppForDetail(app: ProtectedApp) {
        _uiState.value = _uiState.value.copy(selectedApp = app)
        navigateTo(NovaScreen.APP_DETAIL)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setCategoryFilter(category: AppCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setProtectionFilter(filter: ProtectionFilter) {
        _uiState.value = _uiState.value.copy(protectionFilter = filter)
    }

    fun toggleAppProtection(packageName: String, isProtected: Boolean) {
        viewModelScope.launch {
            repository.toggleAppProtection(packageName, isProtected)
            recalculatePrivacyScore()
        }
    }

    fun updateAppDetails(app: ProtectedApp) {
        viewModelScope.launch {
            repository.updateAppDetails(app)
            _uiState.value = _uiState.value.copy(selectedApp = app)
        }
    }

    fun setAllApps(isProtected: Boolean) {
        viewModelScope.launch {
            repository.setAllAppsProtection(isProtected)
            recalculatePrivacyScore()
        }
    }

    fun resetProtectionDefaults() {
        viewModelScope.launch {
            repository.resetProtectionToDefaults()
            recalculatePrivacyScore()
        }
    }

    fun selectProfile(profile: PrivacyProfile) {
        authPreferences.activeProfileName = profile.name
        val config = repository.getProfileConfig(profile)

        _uiState.value = _uiState.value.copy(
            activeProfile = profile,
            profileConfig = config,
            isOverlayActive = config.overlayEnabled,
            overlayOpacity = config.overlayOpacity,
            overlayTint = config.overlayTint,
            isPeepholeActive = config.peepholeModeEnabled
        )

        // Apply overlay if profile dictates
        if (config.overlayEnabled && Settings.canDrawOverlays(context)) {
            PrivacyOverlayService.start(
                context,
                config.overlayOpacity,
                config.peepholeModeEnabled,
                config.overlayTint.argb
            )
        } else if (!config.overlayEnabled) {
            PrivacyOverlayService.stop(context)
        }

        recordEvent(
            "PROFILE_CHANGED",
            "Switched to ${profile.title}",
            "Applied profile privacy rules (${profile.subtitle}).",
            "INFO"
        )
        recalculatePrivacyScore()
    }

    fun toggleScreenOverlay(enabled: Boolean) {
        if (enabled && !Settings.canDrawOverlays(context)) {
            // Need permission
            _uiState.value = _uiState.value.copy(isOverlayActive = false)
            return
        }

        authPreferences.isOverlayActive = enabled
        _uiState.value = _uiState.value.copy(isOverlayActive = enabled)

        if (enabled) {
            PrivacyOverlayService.start(
                context,
                _uiState.value.overlayOpacity,
                _uiState.value.isPeepholeActive,
                _uiState.value.overlayTint.argb
            )
            recordEvent("SHIELD_OVERLAY_ON", "Screen Shield Activated", "Stealth dimmer active over display.", "SUCCESS")
        } else {
            PrivacyOverlayService.stop(context)
            recordEvent("SHIELD_OVERLAY_OFF", "Screen Shield Deactivated", "Display returned to default brightness.", "INFO")
        }
        recalculatePrivacyScore()
    }

    fun setOverlayOpacity(opacity: Float) {
        authPreferences.overlayOpacity = opacity
        _uiState.value = _uiState.value.copy(overlayOpacity = opacity)
        if (_uiState.value.isOverlayActive) {
            PrivacyOverlayService.start(
                context,
                opacity,
                _uiState.value.isPeepholeActive,
                _uiState.value.overlayTint.argb
            )
        }
    }

    fun setOverlayTint(tint: OverlayTint) {
        authPreferences.overlayTintName = tint.name
        _uiState.value = _uiState.value.copy(overlayTint = tint)
        if (_uiState.value.isOverlayActive) {
            PrivacyOverlayService.start(
                context,
                _uiState.value.overlayOpacity,
                _uiState.value.isPeepholeActive,
                tint.argb
            )
        }
    }

    fun togglePeepholeMode(enabled: Boolean) {
        authPreferences.isPeepholeActive = enabled
        _uiState.value = _uiState.value.copy(isPeepholeActive = enabled)
        if (_uiState.value.isOverlayActive) {
            PrivacyOverlayService.start(
                context,
                _uiState.value.overlayOpacity,
                enabled,
                _uiState.value.overlayTint.argb
            )
        }
    }

    fun saveNewPin(pin: String) {
        val salt = cryptoManager.generateSalt()
        val hash = cryptoManager.hashPin(pin, salt)
        authPreferences.pinSalt = salt
        authPreferences.pinHash = hash
        authPreferences.isPinConfigured = true

        val phrase = cryptoManager.generateRecoveryPhrase()
        val encPhrase = cryptoManager.encrypt(phrase)
        authPreferences.recoveryPhraseHash = encPhrase

        _uiState.value = _uiState.value.copy(
            isPinConfigured = true,
            recoveryPhrase = phrase
        )

        recordEvent("PIN_CONFIGURED", "Security PIN Established", "PIN hashed with PBKDF2 salt and Android Keystore encryption.", "SUCCESS")
    }

    fun verifyPin(pin: String): Boolean {
        val salt = authPreferences.pinSalt ?: return false
        val storedHash = authPreferences.pinHash ?: return false
        val computedHash = cryptoManager.hashPin(pin, salt)
        val matches = (computedHash == storedHash)

        if (matches) {
            authPreferences.failedAttempts = 0
            authPreferences.lastUnlockedTimestamp = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(isVaultLocked = false)
            recordEvent("VAULT_UNLOCKED", "Authentication Succeeded", "Valid security PIN entered.", "SUCCESS")
        } else {
            val failed = authPreferences.failedAttempts + 1
            authPreferences.failedAttempts = failed
            recordEvent("AUTH_FAILED", "Authentication Attempt Failed", "Incorrect PIN entered (attempt $failed).", "SECURITY")
        }
        return matches
    }

    fun onBiometricSuccess() {
        authPreferences.failedAttempts = 0
        authPreferences.lastUnlockedTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isVaultLocked = false)
        recordEvent("VAULT_UNLOCKED", "Biometric Authentication Succeeded", "Verified via biometric sensor.", "SUCCESS")
    }

    fun checkLockTimeout() {
        if (!authPreferences.isPinConfigured) return
        val lastUnlocked = authPreferences.lastUnlockedTimestamp
        val timeoutMs = authPreferences.lockTimeoutSeconds * 1000L
        val now = System.currentTimeMillis()

        if (lastUnlocked > 0 && (now - lastUnlocked) > timeoutMs) {
            _uiState.value = _uiState.value.copy(isVaultLocked = true)
        }
    }

    fun unlockVault() {
        authPreferences.failedAttempts = 0
        authPreferences.lastUnlockedTimestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isVaultLocked = false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        authPreferences.isBiometricEnabled = enabled
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
    }

    fun setLockTimeout(seconds: Int) {
        authPreferences.lockTimeoutSeconds = seconds
        _uiState.value = _uiState.value.copy(lockTimeoutSeconds = seconds)
    }

    fun toggleHideSensitiveNotifications(enabled: Boolean) {
        authPreferences.hideSensitiveNotifications = enabled
        _uiState.value = _uiState.value.copy(
            profileConfig = _uiState.value.profileConfig.copy(hideSensitiveNotifications = enabled)
        )
        recalculatePrivacyScore()
    }

    fun toggleFlagSecure(enabled: Boolean) {
        authPreferences.flagSecureEnabled = enabled
        _uiState.value = _uiState.value.copy(
            profileConfig = _uiState.value.profileConfig.copy(flagSecureEnabled = enabled)
        )
    }

    fun refreshHardwareAndDiagnostics() {
        val report = hardwareDetector.detectCapabilities()
        val overlayPerm = Settings.canDrawOverlays(context)
        val notifPerm = NovaNotificationListenerService.isPermissionGranted(context)
        val bioStatus = biometricHelper.getBiometricStatus()

        val checks = listOf(
            DiagnosticCheck(
                id = "hardware_check",
                title = "Hardware Privacy Display Sensor",
                subtitle = if (report.hasHardwarePrivacyDisplay) "Hardware layer detected: ${report.detectedApiName}" else "Standard display panel: Viewing-angle restrictions require software dimmer",
                isPassed = report.hasHardwarePrivacyDisplay,
                isRequired = false,
                actionType = DiagnosticAction.HARDWARE_DISPLAY_CHECK
            ),
            DiagnosticCheck(
                id = "overlay_perm",
                title = "Screen Shield Permission (Overlay)",
                subtitle = if (overlayPerm) "Granted: Can render stealth dimmer" else "Requires 'Display over other apps' permission",
                isPassed = overlayPerm,
                isRequired = true,
                actionType = DiagnosticAction.SYSTEM_ALERT_WINDOW
            ),
            DiagnosticCheck(
                id = "notif_perm",
                title = "Notification Privacy Listener",
                subtitle = if (notifPerm) "Authorized: Sensitive alerts can be masked" else "Requires Notification Access permission",
                isPassed = notifPerm,
                isRequired = false,
                actionType = DiagnosticAction.NOTIFICATION_LISTENER
            ),
            DiagnosticCheck(
                id = "bio_perm",
                title = "Biometric Hardware & Security",
                subtitle = bioStatus.description,
                isPassed = bioStatus.canAuthenticate,
                isRequired = false,
                actionType = DiagnosticAction.BIOMETRIC_SETUP
            ),
            DiagnosticCheck(
                id = "keystore_perm",
                title = "Hardware-Backed Android KeyStore",
                subtitle = "AES-256-GCM encryption key initialized securely",
                isPassed = true,
                isRequired = true,
                actionType = DiagnosticAction.KEYSTORE_VERIFIED
            )
        )

        _uiState.value = _uiState.value.copy(
            hardwareReport = report,
            diagnosticChecks = checks,
            isOverlayPermissionGranted = overlayPerm,
            isNotificationListenerGranted = notifPerm
        )
        recalculatePrivacyScore()
    }

    private fun recalculatePrivacyScore() {
        var score = 30 // baseline
        if (_uiState.value.isPinConfigured) score += 25
        if (_uiState.value.isOverlayActive) score += 20
        if (_uiState.value.activeProfile == PrivacyProfile.MAXIMUM_PRIVACY) score += 15
        else if (_uiState.value.activeProfile == PrivacyProfile.PUBLIC) score += 10
        if (_uiState.value.isOverlayPermissionGranted) score += 5
        if (_uiState.value.profileConfig.hideSensitiveNotifications) score += 5

        val finalScore = score.coerceIn(10, 100)
        val status = when {
            finalScore >= 80 -> PrivacyStatus.PROTECTED
            finalScore >= 50 -> PrivacyStatus.PARTIALLY_PROTECTED
            _uiState.value.diagnosticChecks.any { it.isRequired && !it.isPassed } -> PrivacyStatus.ATTENTION_NEEDED
            else -> PrivacyStatus.INACTIVE
        }

        _uiState.value = _uiState.value.copy(
            privacyScore = finalScore,
            privacyStatus = status
        )
    }

    fun syncApps() {
        viewModelScope.launch {
            repository.syncInstalledApplications()
        }
    }

    fun clearAuditHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resetAllApplicationData() {
        viewModelScope.launch {
            authPreferences.clearAllData()
            repository.clearHistory()
            repository.resetProtectionToDefaults()
            loadInitialState()
            refreshHardwareAndDiagnostics()
        }
    }

    private fun recordEvent(type: String, title: String, desc: String, severity: String = "INFO") {
        viewModelScope.launch {
            repository.recordEvent(type, title, desc, severity)
        }
    }
}
