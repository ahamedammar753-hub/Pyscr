package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.components.PinLockDialog
import com.example.presentation.components.PinMode
import com.example.presentation.navigation.NovaScreen
import com.example.presentation.screens.AboutScreen
import com.example.presentation.screens.AppDetailScreen
import com.example.presentation.screens.AuthSettingsScreen
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.DiagnosticsScreen
import com.example.presentation.screens.HardwareCompatScreen
import com.example.presentation.screens.HistoryScreen
import com.example.presentation.screens.NotificationSettingsScreen
import com.example.presentation.screens.OnboardingScreen
import com.example.presentation.screens.ProfilesScreen
import com.example.presentation.screens.ProtectedAppsScreen
import com.example.presentation.screens.SplashScreen
import com.example.presentation.viewmodel.NovaViewModel
import com.example.presentation.viewmodel.NovaViewModelFactory
import com.example.ui.theme.NovaGuardTheme
import com.example.ui.theme.NovaVoidBlack

class MainActivity : FragmentActivity() {

    private var isAppLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = NovaApplication.instance

        // Apply FLAG_SECURE if enabled in preferences
        if (app.authPreferences.flagSecureEnabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        setContent {
            val viewModel: NovaViewModel = viewModel(
                factory = NovaViewModelFactory(
                    context = applicationContext,
                    repository = app.repository,
                    authPreferences = app.authPreferences,
                    cryptoManager = app.keystoreCryptoManager,
                    biometricHelper = app.biometricHelper
                )
            )

            NovaGuardTheme(darkTheme = true) {
                NovaAppContent(
                    activity = this@MainActivity,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun NovaAppContent(
    activity: FragmentActivity,
    viewModel: NovaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allApps by viewModel.allProtectedApps.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val recentEvents by viewModel.privacyEvents.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkLockTimeout()
                viewModel.refreshHardwareAndDiagnostics()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // System Back Press handling
    BackHandler(enabled = uiState.currentScreen != NovaScreen.DASHBOARD && uiState.currentScreen != NovaScreen.SPLASH) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = NovaVoidBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                NovaScreen.SPLASH -> {
                    SplashScreen(
                        isOnboardingDone = NovaApplication.instance.authPreferences.isOnboardingCompleted,
                        onNavigate = { screen -> viewModel.navigateTo(screen, clearStack = true) }
                    )
                }
                NovaScreen.ONBOARDING -> {
                    OnboardingScreen(
                        onComplete = { viewModel.completeOnboarding() }
                    )
                }
                NovaScreen.DASHBOARD -> {
                    DashboardScreen(
                        state = uiState,
                        allApps = allApps,
                        recentEvents = recentEvents,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onToggleOverlay = { enabled -> viewModel.toggleScreenOverlay(enabled) },
                        onTogglePeephole = { enabled -> viewModel.togglePeepholeMode(enabled) },
                        onOpacityChange = { opacity -> viewModel.setOverlayOpacity(opacity) },
                        onSelectProfile = { profile -> viewModel.selectProfile(profile) }
                    )
                }
                NovaScreen.PROTECTED_APPS -> {
                    ProtectedAppsScreen(
                        apps = filteredApps,
                        searchQuery = uiState.searchQuery,
                        selectedCategory = uiState.selectedCategory,
                        protectionFilter = uiState.protectionFilter,
                        onSearchChange = { q -> viewModel.setSearchQuery(q) },
                        onCategorySelect = { cat -> viewModel.setCategoryFilter(cat) },
                        onFilterSelect = { filter -> viewModel.setProtectionFilter(filter) },
                        onToggleProtection = { pkg, isProt -> viewModel.toggleAppProtection(pkg, isProt) },
                        onSelectAll = { viewModel.setAllApps(true) },
                        onClearAll = { viewModel.setAllApps(false) },
                        onResetDefaults = { viewModel.resetProtectionDefaults() },
                        onAppClick = { app -> viewModel.selectAppForDetail(app) },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.APP_DETAIL -> {
                    uiState.selectedApp?.let { app ->
                        AppDetailScreen(
                            app = app,
                            onSave = { updated -> viewModel.updateAppDetails(updated) },
                            onBack = { viewModel.navigateBack() }
                        )
                    } ?: run {
                        viewModel.navigateTo(NovaScreen.PROTECTED_APPS)
                    }
                }
                NovaScreen.PROFILES -> {
                    ProfilesScreen(
                        activeProfile = uiState.activeProfile,
                        onSelectProfile = { profile -> viewModel.selectProfile(profile) },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.AUTH_SETTINGS -> {
                    AuthSettingsScreen(
                        state = uiState,
                        onSavePin = { pin -> viewModel.saveNewPin(pin) },
                        onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                        onToggleBiometric = { bio -> viewModel.setBiometricEnabled(bio) },
                        onSetLockTimeout = { sec -> viewModel.setLockTimeout(sec) },
                        onTestBiometric = {
                            NovaApplication.instance.biometricHelper.showBiometricPrompt(
                                activity = activity,
                                onSuccess = {
                                    viewModel.onBiometricSuccess()
                                },
                                onError = { _ -> }
                            )
                        },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.NOTIFICATION_SETTINGS -> {
                    NotificationSettingsScreen(
                        state = uiState,
                        allApps = allApps,
                        onToggleHideSensitive = { hide -> viewModel.toggleHideSensitiveNotifications(hide) },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.HARDWARE_COMPAT -> {
                    HardwareCompatScreen(
                        report = uiState.hardwareReport,
                        onRefresh = { viewModel.refreshHardwareAndDiagnostics() },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.DIAGNOSTICS -> {
                    DiagnosticsScreen(
                        checks = uiState.diagnosticChecks,
                        onRefresh = { viewModel.refreshHardwareAndDiagnostics() },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.HISTORY -> {
                    HistoryScreen(
                        events = recentEvents,
                        onClearHistory = { viewModel.clearAuditHistory() },
                        onBack = { viewModel.navigateBack() }
                    )
                }
                NovaScreen.ABOUT -> {
                    AboutScreen(
                        onResetAllData = { viewModel.resetAllApplicationData() },
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }

            if (uiState.isVaultLocked && uiState.isPinConfigured) {
                PinLockDialog(
                    mode = PinMode.VERIFY,
                    onSuccess = {
                        viewModel.unlockVault()
                    },
                    onDismiss = {
                        activity.finish()
                    },
                    onBiometricClick = if (uiState.isBiometricEnabled) {
                        {
                            NovaApplication.instance.biometricHelper.showBiometricPrompt(
                                activity = activity,
                                onSuccess = { viewModel.onBiometricSuccess() },
                                onError = { _ -> }
                            )
                        }
                    } else null,
                    verifyPinHash = { pin -> viewModel.verifyPin(pin) }
                )
            }
        }
    }
}
