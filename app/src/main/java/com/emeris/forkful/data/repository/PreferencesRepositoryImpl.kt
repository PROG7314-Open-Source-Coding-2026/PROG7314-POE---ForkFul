package com.emeris.forkful.data.repository

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.data.mapper.toRequest
import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences
import com.emeris.forkful.domain.repository.PreferencesRepository

//Prefs repo impl
class PreferencesRepositoryImpl(
    private val api: ForkfulApi,
    private val sessionManager: SessionManager
) : PreferencesRepository {

    override suspend fun getPreferences(): Result<UserPreferences> = runCatching {
        ForkfulLogger.logNetwork("user-preferences", "GET")
        val dto = api.userPreferences()
        sessionManager.updatePrefs { current ->
            current.copy(
                theme = dto.theme,
                language = dto.language,
                notificationsEnabled = dto.notificationsEnabled,
                biometricLockEnabled = dto.biometricLockEnabled
            )
        }
        dto.toDomain()
    }

    override suspend fun updatePreferences(update: PreferencesUpdate): Result<Boolean> = runCatching {
        ForkfulLogger.logNetwork("user-preferences", "PUT ${update.copy(seedCuisines = null)}")
        api.updateUserPreferences(update.toRequest())
        update.theme?.let { theme -> sessionManager.updatePrefs { it.copy(theme = theme) } }
        update.language?.let { lang -> sessionManager.updatePrefs { it.copy(language = lang) } }
        update.notificationsEnabled?.let { enabled ->
            sessionManager.updatePrefs { it.copy(notificationsEnabled = enabled) }
        }
        update.biometricLockEnabled?.let { enabled ->
            sessionManager.updatePrefs { it.copy(biometricLockEnabled = enabled) }
        }
        update.onboarded?.let { onboarded -> sessionManager.setOnboarded(onboarded) }
        true
    }
}
