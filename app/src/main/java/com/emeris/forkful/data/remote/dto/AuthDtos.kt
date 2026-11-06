package com.emeris.forkful.data.remote.dto

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
