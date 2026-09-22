package com.emeris.forkful.domain.model

enum class NotificationType {
    EXPIRY_ALERT,
    ACTION_REQUIRED,
    PREP_REMINDER,
    PANTRY_MATCH
}

data class NotificationModel(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val type: NotificationType,
    val isUnread: Boolean = false
)