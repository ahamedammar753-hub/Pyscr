package com.example.platform

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.provider.Settings
import android.view.Display
import com.example.domain.model.HardwareReport
import java.lang.reflect.Method

class HardwareDisplayDetector(private val context: Context) {

    fun detectCapabilities(): HardwareReport {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val defaultDisplay = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)

        val refreshRate = defaultDisplay?.refreshRate ?: 60.0f
        val mode = defaultDisplay?.mode
        val resolution = if (mode != null) "${mode.physicalWidth} x ${mode.physicalHeight}" else "Unknown"

        // Detailed panel heuristic
        val panelTech = if (Build.MANUFACTURER.contains("samsung", ignoreCase = true) ||
            Build.BRAND.contains("samsung", ignoreCase = true) ||
            Build.HARDWARE.contains("qcom", ignoreCase = true)
        ) {
            "Dynamic AMOLED 2X / Super AMOLED"
        } else {
            "High-Refresh OLED / IPS LCD"
        }

        // Check for official OEM Hardware Privacy Display APIs or private system features
        val oemDetection = checkOemPrivacyDisplaySupport()

        val isSupported = oemDetection.supported
        val apiName = oemDetection.apiName
        val intensityLevels = oemDetection.levels

        val honestySummary = if (isSupported) {
            "Hardware Privacy Display Detected ($apiName). Electronic viewing angle attenuation can be modulated via hardware layers."
        } else {
            "Standard Display Panel ($panelTech). No physical privacy-display hardware or public OEM privacy display API is detected."
        }

        val technicalExplanation = """
            PHYSICAL REALITY VS. SOFTWARE FILTER:
            1. Physical Privacy Displays (e.g. micro-louver technology or dual-cell LCD/OLED light-collimating polarizers) restrict the physical emission cone of subpixels to ~30-45 degrees.
            2. Standard smartphone OLED and IPS panels are engineered for ultra-wide viewing angles (>170 degrees) using isotropic light diffusion.
            3. Software overlays and filters (such as Nova Screen Guard's Stealth Dimmer) darken pixels and compress luminance contrast. While effective at deterring casual shoulder-surfers in public spaces, a software overlay physically cannot prevent light photons from emitting laterally from an ordinary panel.
            4. Nova Screen Guard will never falsely claim your standard panel is physically masked when viewing from the side.
        """.trimIndent()

        return HardwareReport(
            hasHardwarePrivacyDisplay = isSupported,
            manufacturer = manufacturer,
            deviceModel = model,
            androidVersion = androidVersion,
            panelTechnology = panelTech,
            refreshRateHz = refreshRate,
            displayMetricsInfo = "$resolution @ ${refreshRate.toInt()}Hz",
            privacyFeatureSupported = isSupported,
            detectedApiName = apiName,
            intensityLevelsSupported = intensityLevels,
            honestySummary = honestySummary,
            technicalExplanation = technicalExplanation
        )
    }

    private data class OemCheckResult(
        val supported: Boolean,
        val apiName: String,
        val levels: List<String>
    )

    private fun checkOemPrivacyDisplaySupport(): OemCheckResult {
        try {
            // Check 1: System settings key (e.g. privacy_screen, display_privacy_mode)
            val secureKey = Settings.Secure.getString(context.contentResolver, "display_privacy_mode")
            if (!secureKey.isNullOrBlank()) {
                return OemCheckResult(
                    supported = true,
                    apiName = "Settings.Secure: display_privacy_mode",
                    levels = listOf("Off", "Medium (45°)", "High (30°)")
                )
            }

            // Check 2: Samsung SecProductFeature / SemFloatingFeature reflection
            val secClasses = listOf(
                "com.samsung.android.feature.SemFloatingFeature",
                "com.samsung.android.feature.SecProductFeature"
            )
            for (className in secClasses) {
                try {
                    val clazz = Class.forName(className)
                    val getInstance: Method? = try { clazz.getMethod("getInstance") } catch (e: Exception) { null }
                    val targetObj = getInstance?.invoke(null) ?: clazz

                    val getBooleanMethod = clazz.getMethod("getBoolean", String::class.java, Boolean::class.javaPrimitiveType)
                    val hasPrivacyDisplay = getBooleanMethod.invoke(
                        targetObj,
                        "SEC_FLOATING_FEATURE_LCD_SUPPORT_PRIVACY_DISPLAY",
                        false
                    ) as? Boolean

                    if (hasPrivacyDisplay == true) {
                        return OemCheckResult(
                            supported = true,
                            apiName = "Samsung Knox/SemFloatingFeature Privacy Display",
                            levels = listOf("Standard (1)", "High Angle Cutoff (2)")
                        )
                    }
                } catch (ignored: Throwable) {
                    // Not a Samsung device or class not available
                }
            }

            // Check 3: System property via reflection
            try {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val getMethod = systemProperties.getMethod("get", String::class.java, String::class.java)
                val prop = getMethod.invoke(null, "ro.vendor.display.privacy_mode", "") as? String
                if (!prop.isNullOrBlank() && prop != "0" && !prop.equals("false", ignoreCase = true)) {
                    return OemCheckResult(
                        supported = true,
                        apiName = "Vendor SystemProperty ro.vendor.display.privacy_mode",
                        levels = listOf("Level 1", "Level 2", "Level 3")
                    )
                }
            } catch (ignored: Throwable) {
                // Not present
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Honest result for standard commercial smartphones
        return OemCheckResult(
            supported = false,
            apiName = "None Detected",
            levels = emptyList()
        )
    }
}
