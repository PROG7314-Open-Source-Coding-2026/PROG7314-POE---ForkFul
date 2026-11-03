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
    val recipe: Recipe? = null,
    val isSaving: Boolean = false,
    val actionMessage: String? = null
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

    //Toggle save bookmark (works both ways)
    fun toggleSave() {
        val snapshot = _state.value
        val recipe = snapshot.recipe ?: return
        if (snapshot.isSaving) return
        val saving = !recipe.isSaved

        //Optimistic heart update
        _state.update { it.copy(isSaving = true, recipe = recipe.copy(isSaved = saving)) }
        viewModelScope.launch {
            val result = if (saving) {
                recipeRepository.recordSwipe(
                    recipe.id,
                    SwipeDirection.RIGHT,
                    recipe.category.lowercase().replace(Regex("[^a-z]"), "").ifBlank { "comfort" }
                ).map { true }
            } else {
                recipeRepository.unsaveRecipe(recipe.id)
            }
            result.onSuccess {
                ForkfulLogger.logAction("RECIPE_BOX", "${if (saving) "Saved" else "Unsaved"} ${recipe.title}")
                _state.update {
                    it.copy(
                        isSaving = false,
                        actionMessage = if (saving) "Saved to your Recipe Box" else "Removed from your Recipe Box"
                    )
                }
            }.onFailure { error ->
                _state.update { current ->
                    current.copy(
                        isSaving = false,
                        recipe = current.recipe?.copy(isSaved = recipe.isSaved),
                        actionMessage = error.message ?: "Could not update your Recipe Box"
                    )
                }
            }
        }
    }

    fun consumeActionMessage() {
        _state.update { it.copy(actionMessage = null) }
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
