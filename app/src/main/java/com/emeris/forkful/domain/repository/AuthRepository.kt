package com.emeris.forkful.domain.repository

import com.emeris.forkful.core.session.Session

//Auth outcome model
data class AuthOutcome(
    val isNewUser: Boolean
)

//Auth repository contract
interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String, email: String?, displayName: String?): Result<AuthOutcome>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthOutcome>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<AuthOutcome>
    fun currentSession(): Session?
    fun signOut()
}
