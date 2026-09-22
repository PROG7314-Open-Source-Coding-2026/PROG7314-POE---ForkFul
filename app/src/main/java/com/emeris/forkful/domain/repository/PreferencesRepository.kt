package com.emeris.forkful.domain.repository

import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences

//Preferences repo contract
interface PreferencesRepository {
    suspend fun getPreferences(): Result<UserPreferences>
    suspend fun updatePreferences(update: PreferencesUpdate): Result<Boolean>
}
