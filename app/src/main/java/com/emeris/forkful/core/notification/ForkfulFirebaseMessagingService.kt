package com.emeris.forkful.core.notification

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
