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
    val isNewUser: Boolean
)
