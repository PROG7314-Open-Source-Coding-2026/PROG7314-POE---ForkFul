package com.emeris.forkful.ui.recipebox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.RecipeBoxFilter
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Forkful - Recipe Box & Bookmarks
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Observable state model representing saved recipes, filter tabs, and text queries.
 *
 * @property isLoading True when retrieving saved bookmarks from local Room cache or Supabase.
 * @property errorMessage Descriptive error message if repository loading fails.
 * @property filter The active filter tab criterion ([RecipeBoxFilter.ALL], [RecipeBoxFilter.COOKED], etc.).
 * @property allRecipes Complete list of bookmarked recipes returned for the current filter.
 * @property searchQuery Debounced search text filtering recipes locally by title.
 * @property searchText Immediate text string reflecting the user's keystrokes.
 */
data class RecipeBoxUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val filter: RecipeBoxFilter = RecipeBoxFilter.ALL,
    val allRecipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val searchText: String = ""
) {
    /** Filtered subset of recipes matching the search query. */
    val displayedRecipes: List<Recipe>
        get() = allRecipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
}

/**
 * ViewModel managing the user's personal Recipe Box, providing bookmark organisation,
 * cooked status filtering, and local in-memory text search.
 *
 * @property recipeRepository Repository handling recipe query and bookmark persistence.
 */
class RecipeBoxViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeBoxUiState())
    val state: StateFlow<RecipeBoxUiState> = _state.asStateFlow()

    private val searchInput = MutableStateFlow("")
    private var hasBeenShown = false

    init {
        load()
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            searchInput.debounce(300).collect { query ->
                _state.update { it.copy(searchQuery = query) }
            }
        }
    }

    /**
     * Loads the recipe collection for the currently selected filter from the repository.
     */
    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            recipeRepository.getRecipeBox(_state.value.filter)
                .onSuccess { recipes ->
                    _state.update { it.copy(isLoading = false, allRecipes = recipes) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load your Recipe Box")
                    }
                }
        }
    }

    /**
     * Re-queries the repository upon screen resume to reflect unsaved or modified bookmarks.
     */
    fun onScreenShown() {
        if (hasBeenShown) load() else hasBeenShown = true
    }

    /**
     * Updates the active filter tab and reloads the recipe box.
     *
     * @param filter Target category filter (e.g., All, Cooked, Quick).
     */
    fun setFilter(filter: RecipeBoxFilter) {
        if (_state.value.filter == filter) return
        _state.update { it.copy(filter = filter) }
        load()
    }

    /**
     * Updates immediate search field state and emits into debounce flow.
     *
     * @param query Raw text entered by the user.
     */
    fun onSearchInput(query: String) {
        _state.update { it.copy(searchText = query) }
        searchInput.value = query
    }
}

//------------------------------End-Of-File------------------------------//
