package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppProtectionEntity
import com.example.data.db.NovaDatabase
import com.example.data.db.PrivacyEventEntity
import com.example.domain.model.AppCategory
import com.example.domain.model.OverlayTint
import com.example.domain.model.PrivacyProfile
import com.example.platform.HardwareDisplayDetector
import com.example.security.AuthPreferences
import com.example.security.KeystoreCryptoManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var inMemoryDb: NovaDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, NovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun `test app launcher label and full name resources`() {
        val appName = context.getString(R.string.app_name)
        val fullName = context.getString(R.string.app_full_name)
        assertEquals("Nova Guard", appName)
        assertEquals("Nova Screen Guard", fullName)
    }

    @Test
    fun `test honest hardware display detection on standard device`() {
        val detector = HardwareDisplayDetector(context)
        val report = detector.detectCapabilities()
        assertNotNull(report)
        // On standard Android / Robolectric, hardware privacy display API is not present
        assertFalse("Standard device must report false for hardware privacy display", report.hasHardwarePrivacyDisplay)
        assertTrue(report.technicalExplanation.contains("PHYSICAL REALITY VS. SOFTWARE FILTER"))
        assertTrue(report.honestySummary.contains("No physical privacy-display hardware"))
    }

    @Test
    fun `test secure salted PIN hashing and verification`() {
        val cryptoManager = KeystoreCryptoManager()
        val pin = "1379"
        val wrongPin = "0000"

        val salt = cryptoManager.generateSalt()
        assertNotNull(salt)
        assertTrue(salt.isNotBlank())

        val hash1 = cryptoManager.hashPin(pin, salt)
        val hash2 = cryptoManager.hashPin(pin, salt)
        val wrongHash = cryptoManager.hashPin(wrongPin, salt)

        assertEquals("Same PIN and salt must produce identical hash", hash1, hash2)
        assertNotEquals("Different PIN must produce different hash", hash1, wrongHash)
        assertFalse("Hash must not contain plain PIN", hash1.contains(pin))
    }

    @Test
    fun `test recovery phrase generator produces distinct words`() {
        val cryptoManager = KeystoreCryptoManager()
        val phrase = cryptoManager.generateRecoveryPhrase()
        val words = phrase.split("-")
        assertEquals(6, words.size)
        assertTrue(words.all { it.isNotBlank() })
    }

    @Test
    fun `test Room database protected apps insertion and query`() = runBlocking {
        val dao = inMemoryDb.novaDao()

        val sampleApp = AppProtectionEntity(
            packageName = "com.banking.app",
            appName = "Citadel Bank",
            isProtected = true,
            category = AppCategory.BANKING.name,
            customShieldOpacity = 0.75f,
            requireBiometrics = true,
            hideNotifications = true
        )

        dao.insertOrUpdateApp(sampleApp)

        val retrieved = dao.getApp("com.banking.app")
        assertNotNull(retrieved)
        assertEquals("Citadel Bank", retrieved?.appName)
        assertTrue(retrieved?.isProtected == true)

        // Toggle protection off
        dao.setAppProtection("com.banking.app", false)
        val updated = dao.getApp("com.banking.app")
        assertFalse(updated?.isProtected == true)

        // Active protected apps flow should be empty
        val activeApps = dao.getActiveProtectedApps().first()
        assertTrue(activeApps.isEmpty())
    }

    @Test
    fun `test Privacy Profile posture definitions`() {
        val everyday = PrivacyProfile.EVERYDAY
        val publicProfile = PrivacyProfile.PUBLIC
        val maxPrivacy = PrivacyProfile.MAXIMUM_PRIVACY

        assertEquals("Everyday Mode", everyday.title)
        assertEquals("Public Mode", publicProfile.title)
        assertEquals("Maximum Privacy Mode", maxPrivacy.title)

        // Verify Overlay tints
        assertEquals("Pitch Black", OverlayTint.PITCH_BLACK.label)
        assertEquals(0xFF000000, OverlayTint.PITCH_BLACK.argb)
    }

    @Test
    fun `test audit event logging in database`() = runBlocking {
        val dao = inMemoryDb.novaDao()

        dao.insertEvent(
            PrivacyEventEntity(
                eventType = "SHIELD_OVERLAY_ON",
                title = "Screen Shield Activated",
                description = "Stealth dimmer enabled at 70% opacity",
                severity = "SUCCESS"
            )
        )

        val events = dao.getAllEvents().first()
        assertEquals(1, events.size)
        assertEquals("Screen Shield Activated", events[0].title)
        assertEquals("SUCCESS", events[0].severity)

        dao.clearAllEvents()
        val emptyEvents = dao.getAllEvents().first()
        assertTrue(emptyEvents.isEmpty())
    }

    @Test
    fun `test encrypt and decrypt roundtrip`() {
        val cryptoManager = KeystoreCryptoManager()
        val originalText = "nova-shield-phrase-12345"
        val encrypted = cryptoManager.encrypt(originalText)
        assertNotNull(encrypted)
        val decrypted = cryptoManager.decrypt(encrypted)
        assertEquals(originalText, decrypted)
    }
}
