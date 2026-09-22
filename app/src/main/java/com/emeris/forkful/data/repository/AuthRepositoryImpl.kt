package com.emeris.forkful.data.repository

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.data.remote.dto.SsoAuthRequest
import com.emeris.forkful.domain.repository.AuthOutcome
import com.emeris.forkful.domain.repository.AuthRepository
import com.emeris.forkful.core.session.Session
import com.emeris.forkful.core.util.Validators
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

/**
 * Talks to the auth-sso Edge Function. On success the server-issued JWT is
 * persisted in the encrypted session store (FR-01).
 */
class AuthRepositoryImpl(
    private val api: ForkfulApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun signInWithGoogle(
        idToken: String,
        email: String?,
        displayName: String?
    ): Result<AuthOutcome> = runCatching {
        ForkfulLogger.logNetwork("auth-sso", "Google SSO sign-in")
        try {
            val response = api.authSso(
                SsoAuthRequest(
                    ssoProvider = "google",
                    idToken = idToken,
                    email = email,
                    displayName = displayName
                )
            )
            sessionManager.saveSession(
                Session(response.token, response.userId, email, displayName)
            )
            AuthOutcome(response.isNewUser)
        } catch (e: HttpException) {
            throw Exception(extractServerError(e), e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthOutcome> =
        authenticateEmail(email, password, mode = "signin", displayName = null)

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthOutcome> = authenticateEmail(email, password, mode = "signup", displayName = displayName)

    private suspend fun authenticateEmail(
        email: String,
        password: String,
        mode: String,
        displayName: String?
    ): Result<AuthOutcome> = runCatching {
        require(Validators.isValidEmail(email)) { "Please enter a valid email address." }
        require(Validators.isValidPassword(password)) { "Password must be at least 6 characters." }
        ForkfulLogger.logNetwork("auth-sso", "Email $mode")
        try {
            val response = api.authSso(
                SsoAuthRequest(
                    ssoProvider = "email",
                    email = email,
                    password = password,
                    displayName = displayName,
                    mode = mode
                )
            )
            sessionManager.saveSession(Session(response.token, response.userId, email, displayName))
            AuthOutcome(response.isNewUser)
        } catch (e: HttpException) {
            throw Exception(extractServerError(e), e)
        }
    }

    private fun extractServerError(e: HttpException): String {
        val errorBody = e.response()?.errorBody()?.string()
        if (!errorBody.isNullOrEmpty()) {
            runCatching {
                val json = Json.parseToJsonElement(errorBody).jsonObject
                json["error"]?.jsonPrimitive?.content?.let { return it }
            }
            return errorBody
        }
        return e.message() ?: "Server error (${e.code()})"
    }

    override fun currentSession(): Session? = sessionManager.getSession()

    override fun signOut() {
        sessionManager.clearSession()
    }
}
