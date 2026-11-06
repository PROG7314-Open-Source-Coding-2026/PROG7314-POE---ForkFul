package com.emeris.forkful.data.repository

/**
 * Forkful - User Preferences Repository Implementation
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Synchronises user theme, language, dietary restrictions, and biometric lock configurations.
 */

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.data.mapper.toRequest
import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences
import com.emeris.forkful.domain.repository.PreferencesRepository

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
        // Update local session storage immediately for snappy UI response
        update.theme?.let { theme -> sessionManager.updatePrefs { it.copy(theme = theme) } }
        update.language?.let { lang -> sessionManager.updatePrefs { it.copy(language = lang) } }
        update.notificationsEnabled?.let { enabled ->
            sessionManager.updatePrefs { it.copy(notificationsEnabled = enabled) }
        }
        update.biometricLockEnabled?.let { enabled ->
            sessionManager.updatePrefs { it.copy(biometricLockEnabled = enabled) }
        }
        update.onboarded?.let { onboarded -> sessionManager.setOnboarded(onboarded) }

        ForkfulLogger.logNetwork("user-preferences", "PUT ${update.copy(seedCuisines = null)}")
        api.updateUserPreferences(update.toRequest())
        true
    }
}

//------------------------------End-Of-File------------------------------//
