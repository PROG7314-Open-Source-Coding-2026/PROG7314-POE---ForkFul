package com.emeris.forkful.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.emeris.forkful.MainActivity
import com.emeris.forkful.R
import com.emeris.forkful.core.logging.ForkfulLogger

/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Helper utility for managing Android notification channels and dispatching
 * high-priority system notifications (pantry expirations, cooking alerts, and suggestions).
 */
object NotificationHelper {

    /** Primary notification channel ID for all kitchen and recipe alerts */
    const val CHANNEL_ID = "forkful_alerts"

    /** User-visible channel title in system notification settings */
    const val CHANNEL_NAME = "Kitchen & Recipe Alerts"

    /** Channel description explaining the alert scope to the user */
    const val CHANNEL_DESC = "Real-time updates for pantry expirations, recipe timers, and smart suggestions"

    /**
     * Initializes the notification channel on Android 8.0 (API 26) and above.
     * Configures high importance, vibration, and application badge display.
     *
     * @param context Application or activity context
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
            ForkfulLogger.logAction("NOTIFICATION", "Created notification channel: $CHANNEL_ID")
        }
    }

    /**
     * Checks whether POST_NOTIFICATIONS runtime permission has been granted (Android 13+).
     *
     * @param context Application or activity context
     * @return True if notification delivery is permitted, false otherwise
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Builds and dispatches a high-priority system notification to the user's status bar.
     * Automatically attaches a PendingIntent that directs the user back to [MainActivity].
     *
     * @param context Context used to build and post notification
     * @param title Header title of the notification
     * @param body Descriptive alert content
     * @param notificationId Unique integer ID for notification deduplication
     */
    fun showNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        createNotificationChannel(context)

        // Launch MainActivity and bring existing task to the front
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            ForkfulLogger.logAction("NOTIFICATION", "Sent real-time notification: '$title'")
        } catch (e: SecurityException) {
            // Handled when Android 13 POST_NOTIFICATIONS permission has been revoked by user
            ForkfulLogger.logAction("NOTIFICATION", "Missing POST_NOTIFICATIONS permission: ${e.message}")
        }
    }
}

//------------------------------End-Of-File------------------------------//
