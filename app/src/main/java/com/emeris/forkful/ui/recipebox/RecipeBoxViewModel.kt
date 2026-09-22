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

//Recipe box state
data class RecipeBoxUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val filter: RecipeBoxFilter = RecipeBoxFilter.ALL,
    val allRecipes: List<Recipe> = emptyList(),
    val searchQuery: String = ""
) {
    val displayedRecipes: List<Recipe>
        get() = allRecipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
}

class RecipeBoxViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeBoxUiState())
    val state: StateFlow<RecipeBoxUiState> = _state.asStateFlow()

    private val searchInput = MutableStateFlow("")

    init {
        load()
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            searchInput.debounce(300).collect { query ->
                _state.update { it.copy(searchQuery = query) }
            }
        }
    }

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

    fun setFilter(filter: RecipeBoxFilter) {
        if (_state.value.filter == filter) return
        _state.update { it.copy(filter = filter) }
        load()
    }

    fun onSearchInput(query: String) {
        searchInput.value = query
    }
}
