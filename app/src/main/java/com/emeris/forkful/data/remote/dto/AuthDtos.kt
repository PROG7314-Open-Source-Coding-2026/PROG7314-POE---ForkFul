package com.emeris.forkful.data.remote.dto

/**
 * Forkful - Authentication Data Transfer Objects (DTOs)
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Data transfer schemas for SSO authentication, sign-up, session responses, and user profile payloads.
 */

import kotlinx.serialization.Serializable

//SSO auth req
@Serializable
data class SsoAuthRequest(
    val ssoProvider: String,
    val idToken: String? = null,
    val email: String? = null,
    val password: String? = null,
    val displayName: String? = null,
    val mode: String? = null
)

//SSO auth res
@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val isNewUser: Boolean,
    val refreshToken: String? = null,
    val expiresAt: Long? = null,
    val onboarded: Boolean? = null
)

//Token refresh req
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

//Token refresh res
@Serializable
data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String,
    val expiresAt: Long? = null,
    val userId: String
)

//------------------------------End-Of-File------------------------------//
