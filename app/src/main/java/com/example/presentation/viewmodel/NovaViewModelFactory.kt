package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.NovaRepository
import com.example.security.AuthPreferences
import com.example.security.BiometricHelper
import com.example.security.KeystoreCryptoManager

class NovaViewModelFactory(
    private val context: Context,
    private val repository: NovaRepository,
    private val authPreferences: AuthPreferences,
    private val cryptoManager: KeystoreCryptoManager,
    private val biometricHelper: BiometricHelper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NovaViewModel::class.java)) {
            return NovaViewModel(
                context = context,
                repository = repository,
                authPreferences = authPreferences,
                cryptoManager = cryptoManager,
                biometricHelper = biometricHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
