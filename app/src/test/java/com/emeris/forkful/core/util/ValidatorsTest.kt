package com.emeris.forkful.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidatorsTest {

    @Test
    fun `valid email addresses pass validation`() {
        assertTrue(Validators.isValidEmail("jamie@forkful.co.za"))
        assertTrue(Validators.isValidEmail("user.name+tag@domain.com"))
        assertTrue(Validators.isValidEmail("a@b.io"))
    }

    @Test
    fun `invalid email addresses fail validation`() {
        assertFalse(Validators.isValidEmail(""))
        assertFalse(Validators.isValidEmail("plainaddress"))
        assertFalse(Validators.isValidEmail("missing@tld"))
        assertFalse(Validators.isValidEmail("@domain.com"))
        assertFalse(Validators.isValidEmail("user@.com"))
    }

    @Test
    fun `passwords shorter than six characters fail validation`() {
        assertFalse(Validators.isValidPassword(""))
        assertFalse(Validators.isValidPassword("abc12"))
    }

    @Test
    fun `passwords of six or more characters pass validation`() {
        assertTrue(Validators.isValidPassword("abc123"))
        assertTrue(Validators.isValidPassword("sup3r-s3cret!"))
    }

    @Test
    fun `isNotBlank rejects empty or blank values`() {
        assertFalse(Validators.isNotBlank("", " "))
        assertFalse(Validators.isNotBlank("flour", ""))
        assertTrue(Validators.isNotBlank("flour", "500"))
    }
}

class PantryUtilsTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 22)

    @Test
    fun `daysUntilExpiry parses iso dates`() {
        assertEquals(3, PantryUtils.daysUntilExpiry("2026-09-25", today))
        assertEquals(0, PantryUtils.daysUntilExpiry("2026-09-22", today))
        assertEquals(-2, PantryUtils.daysUntilExpiry("2026-09-20", today))
    }

    @Test
    fun `daysUntilExpiry returns null for absent or malformed dates`() {
        assertNull(PantryUtils.daysUntilExpiry(null, today))
        assertNull(PantryUtils.daysUntilExpiry("", today))
        assertNull(PantryUtils.daysUntilExpiry("tomorrow", today))
        assertNull(PantryUtils.daysUntilExpiry("22/09/2026", today))
    }

    @Test
    fun `items inside the threshold are flagged as expiring soon`() {
        assertTrue(PantryUtils.isExpiringSoon(0))
        assertTrue(PantryUtils.isExpiringSoon(1))
        assertTrue(PantryUtils.isExpiringSoon(2))
        assertFalse(PantryUtils.isExpiringSoon(3))
        assertFalse(PantryUtils.isExpiringSoon(null))
    }

    @Test
    fun `negative days are flagged as expired not expiring soon`() {
        assertTrue(PantryUtils.isExpired(-1))
        assertFalse(PantryUtils.isExpiringSoon(-1))
    }

    @Test
    fun `expiry labels are human friendly`() {
        assertEquals("Expired", PantryUtils.expiryLabel(-1))
        assertEquals("Expires today", PantryUtils.expiryLabel(0))
        assertEquals("1 day left", PantryUtils.expiryLabel(1))
        assertEquals("4 days left", PantryUtils.expiryLabel(4))
        assertEquals("", PantryUtils.expiryLabel(null))
    }
}
