package com.emeris.forkful.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.util.Validators
import com.emeris.forkful.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Forkful - Authentication & Security Subsystem
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Sealed interface modeling discrete login and credential verification states.
 */
sealed interface LoginUiState {
    /** Default resting state awaiting user input. */
    data object Idle : LoginUiState

    /** Active network verification state displaying spinner. */
    data object Loading : LoginUiState

    /**
     * Successful authentication state.
     *
     * @property needsOnboarding True if the user has not yet configured dietary profile and mood preferences.
     */
    data class Authenticated(val needsOnboarding: Boolean) : LoginUiState

    /**
     * Authentication failure state.
     *
     * @property message Human-readable error description.
     */
    data class Error(val message: String) : LoginUiState
}

/**
 * ViewModel managing user authentication workflows, including Google OAuth SSO,
 * email/password registration, input validation, and post-authentication routing.
 *
 * @property authRepository Repository managing Supabase Auth tokens and session lifecycles.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /**
     * Initiates Google Single Sign-On using an ID token from Credential Manager.
     *
     * @param idToken OpenID Connect token returned by Google Play Services.
     * @param email User's primary email address.
     * @param displayName User's public profile name.
     */
    fun signInWithGoogle(idToken: String, email: String?, displayName: String?) {
        if (_state.value is LoginUiState.Loading) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken, email, displayName)
                .onSuccess { outcome ->
                    ForkfulLogger.logAction("LOGIN", "Google SSO success (needsOnboarding=${outcome.needsOnboarding})")
                    _state.value = LoginUiState.Authenticated(outcome.needsOnboarding)
                }
                .onFailure { error ->
                    ForkfulLogger.logAction("LOGIN", "Google SSO failed: ${error.message}")
                    _state.value = LoginUiState.Error(error.message ?: "Sign-in failed")
                }
        }
    }

    /**
     * Authenticates an existing user using email address and password.
     *
     * @param email Clean email string.
     * @param password User account password.
     */
    fun signInWithEmail(email: String, password: String) {
        val validationMessage = validateCredentials(email, password)
        if (validationMessage != null) {
            _state.value = LoginUiState.Error(validationMessage)
            return
        }
        launchEmailAuth(email, password, mode = "signin", displayName = null)
    }

    /**
     * Registers a new user account with email, password, and display name.
     *
     * @param email Target registration email.
     * @param password Strong account password.
     * @param displayName User full name or handle.
     */
    fun signUpWithEmail(email: String, password: String, displayName: String) {
        val validationMessage = validateCredentials(email, password)
        if (validationMessage != null) {
            _state.value = LoginUiState.Error(validationMessage)
            return
        }
        launchEmailAuth(email, password, mode = "signup", displayName = displayName)
    }

    /**
     * Validates email format and password strength according to safety rules.
     */
    private fun validateCredentials(email: String, password: String): String? = when {
        !Validators.isValidEmail(email) ->
            "Please enter a valid email address."
        !Validators.isValidPassword(password) ->
            "Password must be at least 6 characters."
        else -> null
    }

    private fun launchEmailAuth(email: String, password: String, mode: String, displayName: String?) {
        if (_state.value is LoginUiState.Loading) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            val result = if (mode == "signup") {
                authRepository.signUpWithEmail(email, password, displayName.orEmpty())
            } else {
                authRepository.signInWithEmail(email, password)
            }
            result
                .onSuccess { outcome ->
                    _state.value = LoginUiState.Authenticated(outcome.needsOnboarding)
                }
                .onFailure { error ->
                    _state.value = LoginUiState.Error(error.message ?: "Sign-in failed")
                }
        }
    }

    /**
     * Surfaces an authentication error originating outside the ViewModel (e.g., Credential Manager cancellations).
     *
     * @param message Error description to display.
     */
    fun showError(message: String) {
        _state.value = LoginUiState.Error(message)
    }

    /**
     * Resets authentication state back to [LoginUiState.Idle].
     */
    fun consumeState() {
        _state.value = LoginUiState.Idle
    }
}

//------------------------------End-Of-File------------------------------//
