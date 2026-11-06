package com.emeris.forkful.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.domain.model.ChoiceCatalog
import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences
import com.emeris.forkful.domain.repository.AuthRepository
import com.emeris.forkful.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Forkful - User Preferences & Security Settings
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Observable state holder representing application configuration, dietary preferences, and security toggles.
 *
 * @property isLoading True when initially querying preferences from data persistence.
 * @property isSaving True while an asynchronous preference update is in-flight.
 * @property errorMessage Error text rendered if preference synchronization fails.
 * @property infoMessage Informational notification displayed upon preference mutation success.
 * @property preferences Active user configuration snapshot (theme, biometric locks, notifications, dietary tags).
 * @property loggedOut True once sign-out completes, signaling navigation back to authentication.
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val preferences: UserPreferences = UserPreferences(
        language = "en",
        dietaryTags = emptyList(),
        notificationsEnabled = true,
        theme = "light",
        onboarded = true,
        biometricLockEnabled = false
    ),
    val loggedOut: Boolean = false
)

/**
 * ViewModel managing user preferences, dark mode toggles, multilingual selections,
 * biometric security flags, session logout, and test harness debug hooks.
 *
 * @property preferencesRepository Repository managing local Room/DataStore and remote preference sync.
 * @property authRepository Repository handling credential teardown and user sign-out.
 * @property sessionManager Optional session controller managing JWT tokens and expiry tests.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    val languageOptions = ChoiceCatalog.languageOptions
    val dietaryOptions = ChoiceCatalog.dietaryOptions

    init {
        load()
    }

    /**
     * Loads the current user preferences from the repository.
     */
    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            preferencesRepository.getPreferences()
                .onSuccess { prefs ->
                    _state.update { it.copy(isLoading = false, preferences = prefs) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load settings")
                    }
                }
        }
    }

    /**
     * Helper executing asynchronous preference mutations and updating state.
     */
    private fun update(update: PreferencesUpdate, info: String? = null) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            preferencesRepository.updatePreferences(update)
                .onSuccess {
                    _state.update { current ->
                        current.copy(
                            isSaving = false,
                            infoMessage = info,
                            preferences = current.preferences.applyUpdate(update)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isSaving = false, errorMessage = error.message ?: "Could not save setting")
                    }
                }
        }
    }

    /**
     * Toggles between system light and dark UI themes.
     *
     * @param enabled True for dark theme; false for light theme.
     */
    fun setDarkMode(enabled: Boolean) {
        update(PreferencesUpdate(theme = if (enabled) "dark" else "light"))
    }

    /**
     * Updates the application language localization preference.
     *
     * @param languageKey BCP 47 language code (e.g., "en", "af", "zu").
     */
    fun setLanguage(languageKey: String) {
        update(PreferencesUpdate(language = languageKey), "Language preference saved")
    }

    /**
     * Toggles system cooking and meal reminder push notifications.
     *
     * @param enabled Whether notifications should be scheduled.
     */
    fun setNotifications(enabled: Boolean) {
        update(PreferencesUpdate(notificationsEnabled = enabled))
    }

    /**
     * Toggles biometric lock authentication prompt upon app resumption.
     *
     * @param enabled Whether fingerprint/face recognition is enforced.
     */
    fun setBiometricLock(enabled: Boolean) {
        update(PreferencesUpdate(biometricLockEnabled = enabled))
    }

    /**
     * Toggles membership of a dietary filter tag in user preferences.
     *
     * @param option The dietary restriction to toggle (e.g., "Halaal", "Vegetarian").
     */
    fun toggleDietary(option: String) {
        val current = _state.value.preferences.dietaryTags.toMutableSet()
        if (!current.add(option)) current.remove(option)
        update(PreferencesUpdate(dietaryTags = current.toList()))
    }

    /**
     * Clears taste profile preference weights, resetting the ML card suggestion engine.
     */
    fun resetTasteProfiles() {
        update(PreferencesUpdate(resetTasteProfiles = true), "Taste profiles reset - your deck will start fresh")
    }

    /**
     * Invalidates active session credentials, clears tokens, and sets logout state.
     */
    fun logout() {
        // Onboarding flag is retained so returning users skip onboarding
        authRepository.signOut()
        _state.update { it.copy(loggedOut = true) }
    }

    /**
     * Debug builds only: forces access token expiration to verify automated refresh flows.
     */
    fun debugExpireAccessToken() {
        sessionManager?.debugBreakAccessToken()
        load() // Next call 401s, refreshes and succeeds
    }

    /**
     * Debug builds only: forces complete token invalidation to verify redirect to Login.
     */
    fun debugExpireSession() {
        sessionManager?.debugBreakAllTokens()
        load() // Refresh is rejected and the app returns to Login
    }

    /**
     * Clears user-facing error and info feedback messages.
     */
    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun UserPreferences.applyUpdate(update: PreferencesUpdate): UserPreferences = copy(
        language = update.language ?: language,
        dietaryTags = update.dietaryTags ?: dietaryTags,
        notificationsEnabled = update.notificationsEnabled ?: notificationsEnabled,
        theme = update.theme ?: theme,
        biometricLockEnabled = update.biometricLockEnabled ?: biometricLockEnabled
    )
}

//------------------------------End-Of-File------------------------------//
