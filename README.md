# NOVA SCREEN GUARD

> **"Your Screen. Your Privacy. Your Control."**

NOVA SCREEN GUARD is a production-oriented Android privacy application built with Kotlin, Jetpack Compose, Material 3, Android Keystore, and Room Database. It delivers a privacy display experience inspired by flagship smartphone privacy controls, pairing honest hardware-display detection with high-contrast software dimming shields and per-app security vaults.

---

## Technical Feasibility & Physical Reality

### 1. Hardware Privacy Displays
- **Physical Reality:** True hardware privacy displays (e.g., dual-cell LCDs or micro-louver collimating polarizers) restrict light photons to an emission cone of ~30°–45° at the subpixel layer.
- **Detection Subsystem:** `HardwareDisplayDetector` scans system properties, display flags, and vendor features (e.g., Samsung Knox `SecProductFeature` / `SemFloatingFeature` reflection and `Settings.Secure.display_privacy_mode`).
- **Honesty Mandate:** The app **never** claims that an ordinary smartphone OLED/IPS panel can physically become black from the side. When running on standard displays, it reports: *"Standard Display Panel — No physical privacy-display hardware detected."*

### 2. Software Privacy Mode (Stealth Dimmer & Peephole Slit)
- For standard devices, Nova Screen Guard utilizes `SYSTEM_ALERT_WINDOW` (`WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`) with `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCH_MODAL` so touches pass directly to underlying apps.
- **Stealth Dimmer:** Adjustable opacity (20% to 90%) with customizable tint filters (Pitch Black, Charcoal Stealth, Cyber Violet, Night Amber) to minimize contrast and frustrate shoulder-surfers.
- **Peephole Focus Slit:** Obscures top and bottom screen regions while leaving a horizontal focus aperture clear for the user.

---

## Core Features & Architecture

### 1. App-Specific Privacy Controls
- Enumerates installed launchable apps via `PackageManager` launcher intent query.
- Categorizes apps into Messaging, Gallery, Banking, Social, Email, Browser, and System.
- Search, multi-category chips, Select All, Clear Selection, and Reset to Defaults.
- Granular per-app detail screen: customizable dimmer opacity, biometric prompt requirement, and notification preview masking.
- Persisted locally in SQLite via **Room Database** (`AppProtectionEntity`, `NovaDao`, `NovaDatabase`).

### 2. Adaptive Privacy Profiles
- **Everyday Mode:** Balanced daily posture, standard notifications, 60-second lock grace timeout, inactive overlay.
- **Public Mode:** Optimized for crowded transit. Activates 70% dimmer with Peephole focus slit, immediate relock, and sensitive notification masking.
- **Maximum Privacy Mode:** Zero-exposure lockdown. 85% opacity dimmer, biometric prompt on every resume, `FLAG_SECURE` window protection, and strict notification masking.
- **Custom Mode:** Granular user control over each individual parameter.

### 3. App-Level Lock & Android Keystore Security
- Hardware-isolated key generation via `AndroidKeyStore` (`AES/GCM/NoPadding`).
- Salted PIN hashing via `PBKDF2WithHmacSHA256` (12,000 iterations). Plaintext PIN is **never** stored.
- Biometric authentication via `BiometricPrompt` (`BIOMETRIC_STRONG or DEVICE_CREDENTIAL`).
- 6-word recovery phrase generated for emergency recovery without exposing secrets.
- Configurable auto-relock timeout (Immediate, 30s, 1m, 5m).

### 4. Privacy Activity Audit History
- Real-time audit log of all privacy events: profile switches, app protection toggles, authentication attempts, shield activations, and masked notifications.
- Complete user control to wipe audit logs or reset application data.

---

## 12 Production Screens
1. **Splash Screen:** Animated cyber shield emblem with electric violet and cyan glow.
2. **Onboarding Screen:** 3 interactive slides detailing physical vs software privacy, app protection, and profiles.
3. **Privacy Dashboard:** Large animated status shield, health score, active profile card, hardware status pill, live dimmer controls, and navigation modules.
4. **Protected Apps:** Filterable, searchable app list with category tabs and batch actions.
5. **App-Specific Settings:** Individual app protection policy configuration.
6. **Privacy Profiles:** Instant switching between Everyday, Public, Maximum Privacy, and Custom modes.
7. **Authentication Settings:** Biometric toggle, PIN setup/change, lock timeout, and recovery phrase.
8. **Notification Privacy Settings:** Masking controls and Android system notification settings guidance.
9. **Hardware Compatibility:** Deep panel diagnostics (geometry, refresh rate, technology, vendor API check) and physical optics disclosure.
10. **Permissions & Diagnostics:** Real-time health check of Overlay, Notification Access, Biometrics, Keystore, and Hardware.
11. **Privacy Activity History:** Detailed event log with severity pills and timestamps.
12. **About & Troubleshooting:** Architecture disclosure, FAQ, zero-telemetry policy, and full data wipe.

---

## Permissions & Least-Privilege
- `SYSTEM_ALERT_WINDOW`: Screen Shield Stealth Dimmer.
- `USE_BIOMETRIC` / `USE_FINGERPRINT`: Biometric vault unlock.
- `POST_NOTIFICATIONS`: Persistent status notification while overlay is active.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`: Required on Android 14+ for overlay foreground services.
- `PACKAGE_USAGE_STATS` (Optional): App detection.
- `BIND_NOTIFICATION_LISTENER_SERVICE` (Optional): Masking sensitive alerts.
- **Zero Internet Permission:** No network calls, no analytics, no external tracking.

---

## Manual Device-Testing Checklist
- [x] Standard Android physical smartphone / emulator (confirms honest "Standard Display" status).
- [x] Device with biometric enrolled (tests BiometricPrompt unlock).
- [x] Device without biometric hardware (verifies graceful fallback to 4-digit PIN).
- [x] App backgrounding and resume (verifies lock timeout).
- [x] Process recreation & screen rotation (retains state, active profile, and search queries).
- [x] Overlay permission flow (correctly launches Settings and activates dimmer when granted).
- [x] Room database persistence (selections remain intact across app restart).
