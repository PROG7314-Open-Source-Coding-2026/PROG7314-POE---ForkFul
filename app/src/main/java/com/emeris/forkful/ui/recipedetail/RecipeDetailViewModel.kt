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

/**
 * Forkful - Culinary Recipe Specifications
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Observable state holder representing recipe details, ingredients, and bookmark states.
 *
 * @property isLoading True while the detailed recipe model is being queried.
 * @property errorMessage Error text if fetching recipe data encounters a failure.
 * @property recipe The fully hydrated recipe domain model.
 * @property isSaving True while bookmark save/unsave network transactions execute.
 * @property actionMessage Transient feedback banner text confirming bookmark state transitions.
 */
data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recipe: Recipe? = null,
    val isSaving: Boolean = false,
    val actionMessage: String? = null
)

/**
 * ViewModel managing detailed recipe inspection, two-way bookmark toggling,
 * optimistic heart animation states, and cooked history recording.
 *
 * @property recipeRepository Repository handling recipe hydration and bookmark persistence.
 */
class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state.asStateFlow()

    /**
     * Loads the detailed recipe entity by ID.
     *
     * @param recipeId Unique identifier of the recipe.
     */
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

    /**
     * Toggles bookmark save status bidirectionally with optimistic UI feedback
     * and automatic rollback upon network failure.
     */
    fun toggleSave() {
        val snapshot = _state.value
        val recipe = snapshot.recipe ?: return
        if (snapshot.isSaving) return
        val saving = !recipe.isSaved

        // Optimistic heart update
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

    /**
     * Clears user feedback action message banner.
     */
    fun consumeActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }

    /**
     * Flags recipe as cooked in user history and triggers completion callback.
     *
     * @param onDone Callback invoked after database commit.
     */
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

//------------------------------End-Of-File------------------------------//
