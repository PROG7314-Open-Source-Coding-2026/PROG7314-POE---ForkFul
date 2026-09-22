package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request body for POST /functions/v1/auth-sso (Planning & Design section 5.2).
 *
 * Google SSO: send [ssoProvider] = "google" and the Google ID token obtained
 * from Android Credential Manager. The Edge Function exchanges it for a
 * server-issued Supabase session JWT.
 *
 * Email: send [ssoProvider] = "email" with [mode] = "signin" | "signup".
 */
@Serializable
data class SsoAuthRequest(
    val ssoProvider: String,
    val idToken: String? = null,
    val email: String? = null,
    val password: String? = null,
    val displayName: String? = null,
    val mode: String? = null
)

/**
 * Response body for POST /functions/v1/auth-sso:
 * `{ token, userId, isNewUser }`.
 */
@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val isNewUser: Boolean
)
