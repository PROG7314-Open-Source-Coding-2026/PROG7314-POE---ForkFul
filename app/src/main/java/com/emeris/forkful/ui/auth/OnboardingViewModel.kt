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

/**
 * Forkful - First-Run User Personalisation
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Observable state model representing multi-step onboarding progression and culinary preferences.
 *
 * @property step Active onboarding wizard page index (0 = Welcome, 1 = Cuisines, 2 = Dietary, 3 = Confirmation).
 * @property selectedCuisines Set of preferred culinary cuisines chosen to seed taste profile recommendations.
 * @property selectedDietary Set of dietary restrictions and allergens (e.g. Vegetarian, Gluten-Free, Halaal).
 * @property displayName User handle or name displayed in personalised greetings.
 * @property isSubmitting True while preferences are being saved to remote backend.
 * @property isComplete True once preferences are committed, allowing entry into main dashboard.
 * @property errorMessage Error text rendered if preference seeding fails.
 */
data class OnboardingUiState(
    val step: Int = 0,
    val selectedCuisines: Set<String> = emptySet(),
    val selectedDietary: Set<String> = emptySet(),
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
) {
    /** True if the active step meets mandatory input requirements to proceed. */
    val canContinue: Boolean
        get() = when (step) {
            1 -> selectedCuisines.isNotEmpty()
            else -> true
        }
}

/**
 * ViewModel orchestrating multi-step initial onboarding, capturing flavor profiles,
 * dietary constraints, and initialising taste recommendation vectors.
 *
 * @property preferencesRepository Repository handling user preferences and profile seeding.
 */
class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    val cuisineChoices = ChoiceCatalog.cuisines
    val dietaryChoices = ChoiceCatalog.dietaryOptions

    /**
     * Advances to the next onboarding step, capping at the final step.
     */
    fun nextStep() {
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(3)) }
    }

    /**
     * Reverts to the previous onboarding step, flooring at zero.
     */
    fun previousStep() {
        _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    /**
     * Toggles inclusion of a culinary preference tag.
     *
     * @param key Unique cuisine key string.
     */
    fun toggleCuisine(key: String) {
        _state.update { current ->
            val next = current.selectedCuisines.toMutableSet()
            if (!next.add(key)) next.remove(key)
            current.copy(selectedCuisines = next)
        }
    }

    /**
     * Toggles inclusion of a dietary allergy or restriction option.
     *
     * @param option The dietary restriction name.
     */
    fun toggleDietary(option: String) {
        _state.update { current ->
            val next = current.selectedDietary.toMutableSet()
            if (!next.add(option)) next.remove(option)
            current.copy(selectedDietary = next)
        }
    }

    /**
     * Persists chosen cuisines and dietary requirements, marking onboarding as completed.
     */
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

//------------------------------End-Of-File------------------------------//
