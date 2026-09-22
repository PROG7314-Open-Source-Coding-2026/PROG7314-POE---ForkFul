package com.emeris.forkful.core.util

import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

//Input validation helpers
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = email.isNotBlank() && EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isNotBlank(vararg values: String): Boolean = values.all { it.isNotBlank() }
}

//Pantry expiry helpers
object PantryUtils {

    const val DEFAULT_EXPIRY_THRESHOLD_DAYS = 2

    //Expiry days calc
    fun daysUntilExpiry(expiryDate: String?, today: LocalDate = LocalDate.now()): Int? {
        if (expiryDate.isNullOrBlank()) return null
        return try {
            ChronoUnit.DAYS.between(today, LocalDate.parse(expiryDate)).toInt()
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

    //Pantry pill label
    fun expiryLabel(daysUntilExpiry: Int?): String = when {
        daysUntilExpiry == null -> ""
        daysUntilExpiry < 0 -> "Expired"
        daysUntilExpiry == 0 -> "Expires today"
        daysUntilExpiry == 1 -> "1 day left"
        else -> "$daysUntilExpiry days left"
    }
}
