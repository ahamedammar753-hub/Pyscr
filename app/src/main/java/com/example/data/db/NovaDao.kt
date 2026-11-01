package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NovaDao {
    @Query("SELECT * FROM protected_apps ORDER BY appName ASC")
    fun getAllProtectedApps(): Flow<List<AppProtectionEntity>>

    @Query("SELECT * FROM protected_apps WHERE isProtected = 1 ORDER BY appName ASC")
    fun getActiveProtectedApps(): Flow<List<AppProtectionEntity>>

    @Query("SELECT * FROM protected_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): AppProtectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: AppProtectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppProtectionEntity>)

    @Update
    suspend fun updateApp(app: AppProtectionEntity)

    @Query("UPDATE protected_apps SET isProtected = :isProtected, lastModifiedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun setAppProtection(packageName: String, isProtected: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE protected_apps SET isProtected = :isProtected, lastModifiedTimestamp = :timestamp")
    suspend fun setAllProtection(isProtected: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM protected_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    // Events
    @Query("SELECT * FROM privacy_events ORDER BY timestamp DESC LIMIT 100")
    fun getAllEvents(): Flow<List<PrivacyEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PrivacyEventEntity)

    @Query("DELETE FROM privacy_events")
    suspend fun clearAllEvents()
}
