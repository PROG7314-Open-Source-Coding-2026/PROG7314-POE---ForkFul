package com.emeris.forkful.ui.recipedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Recipe detail state
data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recipe: Recipe? = null
)

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state.asStateFlow()

    fun load(recipeId: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            recipeRepository.getRecipeDetail(recipeId)
                .onSuccess { recipe ->
                    _state.update { it.copy(isLoading = false, recipe = recipe) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load recipe")
                    }
                }
        }
    }

    //Toggle save bookmark
    fun toggleSave() {
        val recipe = _state.value.recipe ?: return
        if (recipe.isSaved) return
        viewModelScope.launch {
            recipeRepository.recordSwipe(
                recipe.id,
                SwipeDirection.RIGHT,
                recipe.category.lowercase().replace(Regex("[^a-z]"), "").ifBlank { "comfort" }
            ).onSuccess {
                _state.update { current ->
                    current.copy(recipe = current.recipe?.copy(isSaved = true))
                }
            }
        }
    }

    fun markCooked(onDone: () -> Unit) {
        val recipe = _state.value.recipe ?: return
        viewModelScope.launch {
            recipeRepository.setCooked(recipe.id, cooked = true)
                .onSuccess {
                    ForkfulLogger.logAction("COOKING_SESSION", "Marked cooked: ${recipe.title}")
                    _state.update { current ->
                        current.copy(recipe = current.recipe?.copy(isCooked = true))
                    }
                    onDone()
                }
        }
    }
}
