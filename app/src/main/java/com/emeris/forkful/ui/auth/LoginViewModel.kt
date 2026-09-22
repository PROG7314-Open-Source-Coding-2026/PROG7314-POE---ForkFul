package com.emeris.forkful.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Authentication UI state (FR-01). */
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Authenticated(val isNewUser: Boolean) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

/**
 * Handles Google SSO and email sign-in/sign-up against the auth-sso Edge
 * Function. The Google ID token itself is obtained in the Composable via
 * Android Credential Manager (requires an Activity); this ViewModel performs
 * the token-for-JWT exchange and session persistence.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun signInWithGoogle(idToken: String, email: String?, displayName: String?) {
        if (_state.value is LoginUiState.Loading) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken, email, displayName)
                .onSuccess { outcome ->
                    ForkfulLogger.logAction("LOGIN", "Google SSO success (isNewUser=${outcome.isNewUser})")
                    _state.value = LoginUiState.Authenticated(outcome.isNewUser)
                }
                .onFailure { error ->
                    ForkfulLogger.logAction("LOGIN", "Google SSO failed: ${error.message}")
                    _state.value = LoginUiState.Error(error.message ?: "Sign-in failed")
                }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        val validationMessage = validateCredentials(email, password)
        if (validationMessage != null) {
            _state.value = LoginUiState.Error(validationMessage)
            return
        }
        launchEmailAuth(email, password, mode = "signin", displayName = null)
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        val validationMessage = validateCredentials(email, password)
        if (validationMessage != null) {
            _state.value = LoginUiState.Error(validationMessage)
            return
        }
        launchEmailAuth(email, password, mode = "signup", displayName = displayName)
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        !com.emeris.forkful.core.util.Validators.isValidEmail(email) ->
            "Please enter a valid email address."
        !com.emeris.forkful.core.util.Validators.isValidPassword(password) ->
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
                    _state.value = LoginUiState.Authenticated(outcome.isNewUser)
                }
                .onFailure { error ->
                    _state.value = LoginUiState.Error(error.message ?: "Sign-in failed")
                }
        }
    }

    fun consumeState() {
        _state.value = LoginUiState.Idle
    }
}
