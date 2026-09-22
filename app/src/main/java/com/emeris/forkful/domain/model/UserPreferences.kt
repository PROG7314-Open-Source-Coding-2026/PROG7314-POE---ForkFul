package com.emeris.forkful.domain.model

//User preferences model
data class UserPreferences(
    val language: String,
    val dietaryTags: List<String>,
    val notificationsEnabled: Boolean,
    val theme: String,
    val onboarded: Boolean,
    val biometricLockEnabled: Boolean,
    val email: String? = null,
    val displayName: String? = null
)

//Preferences update model
data class PreferencesUpdate(
    val language: String? = null,
    val dietaryTags: List<String>? = null,
    val notificationsEnabled: Boolean? = null,
    val theme: String? = null,
    val onboarded: Boolean? = null,
    val biometricLockEnabled: Boolean? = null,
    val seedCuisines: List<String>? = null,
    val resetTasteProfiles: Boolean? = null
)
