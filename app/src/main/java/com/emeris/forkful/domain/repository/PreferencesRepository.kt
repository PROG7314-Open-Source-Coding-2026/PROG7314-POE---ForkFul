package com.emeris.forkful.domain.repository

/**
 * Forkful - User Preferences Domain Repository Contract
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain interface declaring operations for retrieving and updating user application preferences.
 */

import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences

//Preferences repo contract
interface PreferencesRepository {
    suspend fun getPreferences(): Result<UserPreferences>
    suspend fun updatePreferences(update: PreferencesUpdate): Result<Boolean>
}

//------------------------------End-Of-File------------------------------//
