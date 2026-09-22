package com.emeris.forkful.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.ChoiceCatalog
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.repository.DeckQuery
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Explore UI state
data class ExploreUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val feed: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val maxPrepTime: Int? = null,
    val minRating: Double? = null,
    val difficulty: String? = null,
    val savedMessage: String? = null
) {
    val hasActiveFilters: Boolean
        get() = maxPrepTime != null || minRating != null || difficulty != null || searchQuery.isNotBlank()
}

//Explore ViewModel
@OptIn(FlowPreview::class)
class ExploreViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    private val searchInput = MutableStateFlow("")

    val moodShelves = ChoiceCatalog.moodShelves
    val cuisineRow = ChoiceCatalog.cuisines.take(5)
    val prepTimeOptions = ChoiceCatalog.prepTimeOptions
    val difficultyOptions = ChoiceCatalog.difficultyOptions

    init {
        loadFeed()
        viewModelScope.launch {
            searchInput.debounce(350).collect { query ->
                if (query != _state.value.searchQuery) {
                    _state.update { it.copy(searchQuery = query) }
                    loadFeed()
                }
            }
        }
    }

    fun loadFeed() {
        val snapshot = _state.value
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            recipeRepository.getDeck(
                DeckQuery(
                    mood = null,
                    limit = 6,
                    maxPrepTime = snapshot.maxPrepTime,
                    minRating = snapshot.minRating,
                    difficulty = snapshot.difficulty,
                    search = snapshot.searchQuery.takeIf { it.isNotBlank() }
                )
            ).onSuccess { feed ->
                _state.update { it.copy(isLoading = false, feed = feed) }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Could not load matches")
                }
            }
        }
    }

    fun onSearchInput(query: String) {
        searchInput.value = query
    }

    fun togglePrepTime(minutes: Int) {
        _state.update { it.copy(maxPrepTime = if (it.maxPrepTime == minutes) null else minutes) }
        loadFeed()
    }

    fun setMinRating(rating: Double?) {
        _state.update { it.copy(minRating = rating) }
        loadFeed()
    }

    fun toggleDifficulty(difficulty: String) {
        _state.update { it.copy(difficulty = if (it.difficulty == difficulty) null else difficulty) }
        loadFeed()
    }

    fun clearFilters() {
        _state.update { it.copy(maxPrepTime = null, minRating = null, difficulty = null) }
        loadFeed()
    }

    //Save recipe bookmark
    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.recordSwipe(
                recipeId = recipe.id,
                direction = SwipeDirection.RIGHT,
                moodProfileKey = recipe.category.lowercase().replace(Regex("[^a-z]"), "")
                    .takeIf { it.isNotBlank() } ?: "comfort"
            ).onSuccess {
                _state.update { current ->
                    current.copy(
                        feed = current.feed.map { if (it.id == recipe.id) it.copy(isSaved = true) else it },
                        savedMessage = "${recipe.title} saved to your Recipe Box"
                    )
                }
            }.onFailure { error ->
                ForkfulLogger.logAction("EXPLORE", "Save failed: ${error.message}")
            }
        }
    }

    fun consumeSavedMessage() {
        _state.update { it.copy(savedMessage = null) }
    }
}
