package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protected_apps")
data class AppProtectionEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isProtected: Boolean,
    val category: String,
    val customShieldOpacity: Float = 0.65f,
    val requireBiometrics: Boolean = true,
    val hideNotifications: Boolean = true,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "privacy_events")
data class PrivacyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: String = "INFO" // INFO, WARNING, SUCCESS, SECURITY
)
