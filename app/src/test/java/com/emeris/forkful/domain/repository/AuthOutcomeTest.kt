package com.emeris.forkful.domain.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthOutcomeTest {

    @Test
    fun `returning user who finished onboarding goes straight to the app`() {
        assertFalse(AuthOutcome.from(isNewUser = false, serverOnboarded = true).needsOnboarding)
    }

    @Test
    fun `returning user who never finished onboarding is sent back to it`() {
        assertTrue(AuthOutcome.from(isNewUser = false, serverOnboarded = false).needsOnboarding)
    }

    @Test
    fun `brand new user is onboarded`() {
        assertTrue(AuthOutcome.from(isNewUser = true, serverOnboarded = false).needsOnboarding)
    }

    @Test
    fun `older backend without the flag falls back to isNewUser`() {
        assertTrue(AuthOutcome.from(isNewUser = true, serverOnboarded = null).needsOnboarding)
        assertFalse(AuthOutcome.from(isNewUser = false, serverOnboarded = null).needsOnboarding)
    }
}

//------------------------------End-Of-File------------------------------//
