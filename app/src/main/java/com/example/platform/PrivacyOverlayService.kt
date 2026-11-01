package com.example.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.security.AuthPreferences

class PrivacyOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayContainer: FrameLayout? = null
    private var isPeepholeMode: Boolean = false
    private var overlayOpacity: Float = 0.60f
    private var overlayTintHex: Long = 0xFF000000

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_PEEPHOLE -> {
                isPeepholeMode = !isPeepholeMode
                updateOverlayView()
            }
            else -> {
                val prefs = AuthPreferences(this)
                overlayOpacity = intent?.getFloatExtra(EXTRA_OPACITY, prefs.overlayOpacity) ?: prefs.overlayOpacity
                isPeepholeMode = intent?.getBooleanExtra(EXTRA_PEEPHOLE, prefs.isPeepholeActive) ?: prefs.isPeepholeActive
                overlayTintHex = intent?.getLongExtra(EXTRA_TINT, 0xFF000000) ?: 0xFF000000
                updateOverlayView()
            }
        }
        return START_STICKY
    }

    private fun updateOverlayView() {
        removeOverlayView()

        val wm = windowManager ?: return
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Allow user touches to pass through overlay to underlying apps
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val container = FrameLayout(this)
        overlayContainer = container

        if (isPeepholeMode) {
            buildPeepholeLayout(container)
        } else {
            // Full screen dimming shield
            val fullFilter = View(this).apply {
                setBackgroundColor(overlayTintHex.toInt())
                alpha = overlayOpacity
            }
            container.addView(
                fullFilter,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            )
        }

        try {
            wm.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildPeepholeLayout(parent: FrameLayout) {
        // Upper obscured block
        val topBlock = View(this).apply {
            setBackgroundColor(overlayTintHex.toInt())
            alpha = (overlayOpacity + 0.25f).coerceAtMost(0.95f)
        }

        // Middle clear viewing slit with subtle neon border
        val slitContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
        }

        // Bottom obscured block
        val bottomBlock = View(this).apply {
            setBackgroundColor(overlayTintHex.toInt())
            alpha = (overlayOpacity + 0.25f).coerceAtMost(0.95f)
        }

        val displayMetrics = resources.displayMetrics
        val totalHeight = displayMetrics.heightPixels
        val slitHeight = (totalHeight * 0.22f).toInt()
        val sideHeight = (totalHeight - slitHeight) / 2

        parent.addView(
            topBlock,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, sideHeight).apply {
                gravity = Gravity.TOP
            }
        )

        parent.addView(
            slitContainer,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, slitHeight).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        )

        parent.addView(
            bottomBlock,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, sideHeight).apply {
                gravity = Gravity.BOTTOM
            }
        )
    }

    private fun removeOverlayView() {
        overlayContainer?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // View already detached
            }
            overlayContainer = null
        }
    }

    override fun onDestroy() {
        removeOverlayView()
        val prefs = AuthPreferences(this)
        prefs.isOverlayActive = false
        super.onDestroy()
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, PrivacyOverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nova Screen Shield Active")
            .setContentText("Stealth privacy dimmer is obscuring display to lateral viewers")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(0, "Deactivate Shield", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nova Screen Guard Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while Screen Shield Overlay is active"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "nova_overlay_channel"
        const val NOTIFICATION_ID = 40401

        const val ACTION_START = "com.example.novaguard.START_OVERLAY"
        const val ACTION_STOP = "com.example.novaguard.STOP_OVERLAY"
        const val ACTION_TOGGLE_PEEPHOLE = "com.example.novaguard.TOGGLE_PEEPHOLE"

        const val EXTRA_OPACITY = "extra_opacity"
        const val EXTRA_PEEPHOLE = "extra_peephole"
        const val EXTRA_TINT = "extra_tint"

        fun start(context: Context, opacity: Float, peephole: Boolean, tintHex: Long) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_OPACITY, opacity)
                putExtra(EXTRA_PEEPHOLE, peephole)
                putExtra(EXTRA_TINT, tintHex)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
