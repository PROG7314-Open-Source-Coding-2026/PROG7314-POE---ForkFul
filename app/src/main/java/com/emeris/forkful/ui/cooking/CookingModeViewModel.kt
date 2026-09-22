package com.emeris.forkful.ui.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Step-by-step Cooking Mode state (FR-14, inspired by SideChef). */
data class CookingUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recipe: Recipe? = null,
    val stepIndex: Int = 0,
    val markedCooked: Boolean = false
) {
    val instructions: List<String> get() = recipe?.instructions.orEmpty()
    val totalSteps: Int get() = instructions.size
    val isLastStep: Boolean get() = totalSteps > 0 && stepIndex == totalSteps - 1
    val progress: Float get() = if (totalSteps == 0) 0f else (stepIndex + 1f) / totalSteps
}

class CookingModeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CookingUiState())
    val state: StateFlow<CookingUiState> = _state.asStateFlow()

    fun load(recipeId: String) {
        if (_state.value.recipe != null) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            recipeRepository.getRecipeDetail(recipeId)
                .onSuccess { recipe ->
                    _state.update {
                        it.copy(isLoading = false, recipe = recipe, stepIndex = 0)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load recipe")
                    }
                }
        }
    }

    fun nextStep() {
        _state.update { state ->
            state.copy(stepIndex = (state.stepIndex + 1).coerceAtMost(state.totalSteps - 1))
        }
        ForkfulLogger.logAction(
            "COOKING_SESSION",
            "Step ${_state.value.stepIndex + 1}/${_state.value.totalSteps}"
        )
    }

    fun previousStep() {
        _state.update { it.copy(stepIndex = (it.stepIndex - 1).coerceAtLeast(0)) }
    }

    fun finishCooking(onDone: () -> Unit) {
        val recipe = _state.value.recipe ?: run { onDone(); return }
        if (_state.value.markedCooked) {
            onDone()
            return
        }
        viewModelScope.launch {
            recipeRepository.setCooked(recipe.id, cooked = true)
                .onSuccess {
                    ForkfulLogger.logAction("COOKING_SESSION", "Marked cooked: ${recipe.title}")
                    _state.update { it.copy(markedCooked = true) }
                    onDone()
                }
                .onFailure { onDone() }
        }
    }
}
