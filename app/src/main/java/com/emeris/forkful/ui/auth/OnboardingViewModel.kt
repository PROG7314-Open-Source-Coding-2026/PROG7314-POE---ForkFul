package com.emeris.forkful.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.ChoiceCatalog
import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Onboarding state
data class OnboardingUiState(
    val step: Int = 0,
    val selectedCuisines: Set<String> = emptySet(),
    val selectedDietary: Set<String> = emptySet(),
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
) {
    val canContinue: Boolean
        get() = when (step) {
            1 -> selectedCuisines.isNotEmpty()
            else -> true
        }
}

//Onboarding ViewModel
class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    val cuisineChoices = ChoiceCatalog.cuisines
    val dietaryChoices = ChoiceCatalog.dietaryOptions

    fun nextStep() {
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(3)) }
    }

    fun previousStep() {
        _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    fun toggleCuisine(key: String) {
        _state.update { current ->
            val next = current.selectedCuisines.toMutableSet()
            if (!next.add(key)) next.remove(key)
            current.copy(selectedCuisines = next)
        }
    }

    fun toggleDietary(option: String) {
        _state.update { current ->
            val next = current.selectedDietary.toMutableSet()
            if (!next.add(option)) next.remove(option)
            current.copy(selectedDietary = next)
        }
    }

    fun submit() {
        val snapshot = _state.value
        if (snapshot.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            preferencesRepository.updatePreferences(
                PreferencesUpdate(
                    seedCuisines = snapshot.selectedCuisines.toList(),
                    dietaryTags = snapshot.selectedDietary.toList(),
                    onboarded = true
                )
            ).onSuccess {
                ForkfulLogger.logAction(
                    "ONBOARDING",
                    "Taste profiles seeded: ${snapshot.selectedCuisines.joinToString()}"
                )
                _state.update { it.copy(isSubmitting = false, isComplete = true) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Could not save your preferences"
                    )
                }
            }
        }
    }
}
