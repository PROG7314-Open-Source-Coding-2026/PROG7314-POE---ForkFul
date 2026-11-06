package com.emeris.forkful.domain.repository

/**
 * Forkful - Authentication Domain Repository Contract
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain interface specifying authentication, credential registration, and token validation contracts.
 */

import com.emeris.forkful.core.session.Session

//Auth outcome model
data class AuthOutcome(
    val isNewUser: Boolean,
    val needsOnboarding: Boolean = isNewUser
) {
    companion object {
        //Server flag wins; older backends only send isNewUser
        fun from(isNewUser: Boolean, serverOnboarded: Boolean?): AuthOutcome = AuthOutcome(
            isNewUser = isNewUser,
            needsOnboarding = serverOnboarded?.not() ?: isNewUser
        )
    }
}

//Auth repository contract
interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String, email: String?, displayName: String?): Result<AuthOutcome>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthOutcome>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<AuthOutcome>
    fun currentSession(): Session?
    fun signOut()
}

//------------------------------End-Of-File------------------------------//
