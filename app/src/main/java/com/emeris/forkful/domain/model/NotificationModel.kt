package com.emeris.forkful.domain.model

/**
 * Forkful - In-App Notification Domain Model
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain representation of notification alerts, timestamps, types, and read statuses.
 */

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

//------------------------------End-Of-File------------------------------//
