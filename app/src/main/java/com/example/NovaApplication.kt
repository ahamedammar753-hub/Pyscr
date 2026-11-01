package com.example

import android.app.Application
import com.example.data.db.NovaDatabase
import com.example.data.repository.NovaRepository
import com.example.security.AuthPreferences
import com.example.security.BiometricHelper
import com.example.security.KeystoreCryptoManager

class NovaApplication : Application() {

    lateinit var database: NovaDatabase
        private set

    lateinit var authPreferences: AuthPreferences
        private set

    lateinit var keystoreCryptoManager: KeystoreCryptoManager
        private set

    lateinit var biometricHelper: BiometricHelper
        private set

    lateinit var repository: NovaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = NovaDatabase.getInstance(this)
        authPreferences = AuthPreferences(this)
        keystoreCryptoManager = KeystoreCryptoManager()
        biometricHelper = BiometricHelper(this)
        repository = NovaRepository(this, database, authPreferences)
    }

    companion object {
        lateinit var instance: NovaApplication
            private set
    }
}
