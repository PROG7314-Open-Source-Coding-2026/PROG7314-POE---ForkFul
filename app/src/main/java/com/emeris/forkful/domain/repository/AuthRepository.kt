package com.emeris.forkful.domain.repository

import com.emeris.forkful.core.session.Session

/** Result of an authentication attempt. */
data class AuthOutcome(
    val isNewUser: Boolean
)

/**
 * Authentication contract (FR-01, FR-02, FR-05): Google SSO exchanges the
 * Credential Manager ID token for a server-issued session JWT; email
 * sign-in/sign-up goes through the same REST endpoint.
 */
interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String, email: String?, displayName: String?): Result<AuthOutcome>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthOutcome>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<AuthOutcome>
    fun currentSession(): Session?
    fun signOut()
}
