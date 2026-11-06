package com.emeris.forkful.core.notification

/**
 * Forkful - Firebase Cloud Messaging (FCM) Service
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Handles incoming push notification messages, token rotations, and foreground notification delivery.
 */

import com.emeris.forkful.core.logging.ForkfulLogger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ForkfulFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ForkfulLogger.logAction("FCM", "New registration token generated: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        ForkfulLogger.logAction("FCM", "Push notification message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Forkful Update"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "You have a new update in your kitchen!"

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body
        )
    }
}

//------------------------------End-Of-File------------------------------//
