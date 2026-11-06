package com.emeris.forkful.domain.model

/**
 * Forkful - User Preferences Domain Model
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain representation of user application configuration and dietary restriction settings.
 */

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

//------------------------------End-Of-File------------------------------//
