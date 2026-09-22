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

/** Settings state (FR-25 .. FR-27). */
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

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    val languageOptions = ChoiceCatalog.languageOptions
    val dietaryOptions = ChoiceCatalog.dietaryOptions

    init {
        load()
    }

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

    fun setDarkMode(enabled: Boolean) {
        update(PreferencesUpdate(theme = if (enabled) "dark" else "light"))
    }

    fun setLanguage(languageKey: String) {
        update(PreferencesUpdate(language = languageKey), "Language preference saved")
    }

    fun setNotifications(enabled: Boolean) {
        update(PreferencesUpdate(notificationsEnabled = enabled))
    }

    fun setBiometricLock(enabled: Boolean) {
        update(PreferencesUpdate(biometricLockEnabled = enabled))
    }

    fun toggleDietary(option: String) {
        val current = _state.value.preferences.dietaryTags.toMutableSet()
        if (!current.add(option)) current.remove(option)
        update(PreferencesUpdate(dietaryTags = current.toList()))
    }

    fun resetTasteProfiles() {
        update(PreferencesUpdate(resetTasteProfiles = true), "Taste profiles reset - your deck will start fresh")
    }

    fun logout() {
        authRepository.signOut()
        sessionManager.setOnboarded(false)
        _state.update { it.copy(loggedOut = true) }
    }

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
