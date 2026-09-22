package com.emeris.forkful.core.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Input validation helpers (NFR-01: every form validates inline and the app
 * never crashes on invalid input).
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = email.isNotBlank() && EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isNotBlank(vararg values: String): Boolean = values.all { it.isNotBlank() }
}

/**
 * Pantry expiry helpers (FR-18 / FR-19: items expiring within the
 * configurable threshold are flagged distinctly).
 */
object PantryUtils {

    const val DEFAULT_EXPIRY_THRESHOLD_DAYS = 2

    /**
     * Parses an ISO-8601 date (yyyy-MM-dd) and returns the whole days until
     * expiry relative to [today], or null when the date is absent/invalid.
     */
    fun daysUntilExpiry(expiryDate: String?, today: LocalDate = LocalDate.now()): Int? {
        if (expiryDate.isNullOrBlank()) return null
        return try {
            java.time.temporal.ChronoUnit.DAYS.between(today, LocalDate.parse(expiryDate)).toInt()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun isExpiringSoon(daysUntilExpiry: Int?, threshold: Int = DEFAULT_EXPIRY_THRESHOLD_DAYS): Boolean {
        return daysUntilExpiry != null && daysUntilExpiry in 0..threshold
    }

    fun isExpired(daysUntilExpiry: Int?): Boolean {
        return daysUntilExpiry != null && daysUntilExpiry < 0
    }

    /** Format the human-facing label for an expiring pantry pill. */
    fun expiryLabel(daysUntilExpiry: Int?): String = when {
        daysUntilExpiry == null -> ""
        daysUntilExpiry < 0 -> "Expired"
        daysUntilExpiry == 0 -> "Expires today"
        daysUntilExpiry == 1 -> "1 day left"
        else -> "$daysUntilExpiry days left"
    }
}
