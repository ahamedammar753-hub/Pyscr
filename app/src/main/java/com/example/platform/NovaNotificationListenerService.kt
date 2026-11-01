package com.example.platform

import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.db.NovaDatabase
import com.example.data.db.PrivacyEventEntity
import com.example.security.AuthPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NovaNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val sbnNonNull = sbn ?: return
        val packageName = sbnNonNull.packageName ?: return

        serviceScope.launch {
            try {
                val db = NovaDatabase.getInstance(applicationContext)
                val app = db.novaDao().getApp(packageName)
                val prefs = AuthPreferences(applicationContext)

                if (app != null && app.isProtected && prefs.hideSensitiveNotifications) {
                    // Record sensitive notification event in audit log
                    val notificationTitle = sbnNonNull.notification.extras.getCharSequence("android.title")?.toString() ?: "Sensitive Content"
                    db.novaDao().insertEvent(
                        PrivacyEventEntity(
                            eventType = "NOTIFICATION_SHIELDED",
                            title = "Notification Masked: ${app.appName}",
                            description = "Sensitive notification preview from ${app.appName} was flagged for privacy obfuscation.",
                            severity = "SECURITY"
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    companion object {
        fun isPermissionGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return flat.contains(packageName)
        }
    }
}
