package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

//User prefs DTO
@Serializable
data class UserPreferencesDto(
    val userId: String,
    val email: String? = null,
    val displayName: String? = null,
    val language: String = "en",
    val dietaryTags: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val theme: String = "light",
    val onboarded: Boolean = false,
    val biometricLockEnabled: Boolean = false
)

//Update prefs req
@Serializable
data class UpdatePreferencesRequest(
    val language: String? = null,
    val dietaryTags: List<String>? = null,
    val notificationsEnabled: Boolean? = null,
    val theme: String? = null,
    val onboarded: Boolean? = null,
    val biometricLockEnabled: Boolean? = null,
    val seedCuisines: List<String>? = null,
    val resetTasteProfiles: Boolean? = null
)
