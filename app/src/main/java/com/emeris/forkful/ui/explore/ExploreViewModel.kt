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

/**
 * Forkful - Recipe Discovery Platform
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Represents the observable UI state for the Explore and Discovery screen.
 *
 * @property isLoading True when an asynchronous recipe fetch is currently running.
 * @property errorMessage A user-facing failure message if deck query encounters an error.
 * @property feed The filtered list of curated recipes matching current criteria.
 * @property searchQuery The debounced search string passed into the repository query.
 * @property searchText The immediate, non-debounced text displayed inside the search bar.
 * @property selectedCuisine Filter key representing the selected culinary or mood category.
 * @property maxPrepTime Filter upper bound in minutes for recipe preparation.
 * @property minRating Filter lower bound for star ratings.
 * @property difficulty Filter token specifying culinary skill level (Easy, Medium, Hard).
 * @property savedMessage Snack message rendered upon saving a recipe to the Recipe Box.
 */
data class ExploreUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val feed: List<Recipe> = emptyList(),
    val searchQuery: String = "", // Debounced value used for the API call
    val searchText: String = "", // What the search box shows, updated on every keystroke
    val selectedCuisine: String? = null,
    val maxPrepTime: Int? = null,
    val minRating: Double? = null,
    val difficulty: String? = null,
    val savedMessage: String? = null
) {
    /**
     * Determines whether any query parameter, filter pill, or category selection is active.
     */
    val hasActiveFilters: Boolean
        get() = selectedCuisine != null || maxPrepTime != null || minRating != null || difficulty != null || searchQuery.isNotBlank()
}

/**
 * ViewModel orchestrating dynamic recipe exploration, debounced query searching,
 * multi-attribute filtering (mood, prep time, difficulty, rating), and instant bookmarking.
 *
 * Implements reactive state emission via Kotlin Coroutines [StateFlow] and flows debounced
 * keystrokes to prevent excessive repository queries during fast text entry.
 *
 * @property recipeRepository Repository powering recipe retrieval and bookmark operations.
 */
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
            // Debounce user keystrokes by 350ms to minimize database/network load
            searchInput.debounce(350).collect { query ->
                if (query != _state.value.searchQuery) {
                    _state.update { it.copy(searchQuery = query) }
                    loadFeed()
                }
            }
        }
    }

    /**
     * Queries the recipe repository with current filters and debounced search parameters,
     * updating state with the filtered result set.
     */
    fun loadFeed() {
        val snapshot = _state.value
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            recipeRepository.getDeck(
                DeckQuery(
                    mood = snapshot.selectedCuisine,
                    limit = 50,
                    maxPrepTime = snapshot.maxPrepTime,
                    minRating = snapshot.minRating,
                    difficulty = snapshot.difficulty,
                    search = snapshot.searchQuery.takeIf { it.isNotBlank() }
                )
            ).onSuccess { feed ->
                val filteredFeed = if (snapshot.selectedCuisine != null) {
                    val target = snapshot.selectedCuisine.lowercase().trim()
                    feed.filter { recipe ->
                        if (target == "vegan") {
                            recipe.dietaryTags.any { it.equals("vegan", ignoreCase = true) } ||
                                    recipe.tags.any { it.contains("vegan", ignoreCase = true) }
                        } else {
                            recipe.category.lowercase().trim() == target
                        }
                    }
                } else {
                    feed
                }
                _state.update { it.copy(isLoading = false, feed = filteredFeed) }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Could not load matches")
                }
            }
        }
    }

    /**
     * Toggles selection of a specific cuisine or mood tag, re-querying the deck.
     *
     * @param cuisineKey Unique key identifier for the chosen culinary category.
     */
    fun toggleCuisine(cuisineKey: String) {
        _state.update {
            it.copy(selectedCuisine = if (it.selectedCuisine == cuisineKey) null else cuisineKey)
        }
        loadFeed()
    }

    /**
     * Handles keystroke input from the search textfield and pushes to debounce flow.
     *
     * @param query Raw text entered by the user.
     */
    fun onSearchInput(query: String) {
        _state.update { it.copy(searchText = query) }
        searchInput.value = query
    }

    /**
     * Toggles preparation duration filter constraint in minutes.
     *
     * @param minutes Maximum allowable prep time in minutes.
     */
    fun togglePrepTime(minutes: Int) {
        _state.update { it.copy(maxPrepTime = if (it.maxPrepTime == minutes) null else minutes) }
        loadFeed()
    }

    /**
     * Sets or clears the minimum recipe user rating filter.
     *
     * @param rating Lower bound star rating, or null to clear constraint.
     */
    fun setMinRating(rating: Double?) {
        _state.update { it.copy(minRating = rating) }
        loadFeed()
    }

    /**
     * Toggles difficulty level filter (e.g. Easy, Medium, Hard).
     *
     * @param difficulty Level descriptor to toggle.
     */
    fun toggleDifficulty(difficulty: String) {
        _state.update { it.copy(difficulty = if (it.difficulty == difficulty) null else difficulty) }
        loadFeed()
    }

    /**
     * Clears all active filters and search strings, resetting the explore feed to defaults.
     */
    fun clearFilters() {
        searchInput.value = ""
        _state.update {
            it.copy(
                selectedCuisine = null,
                maxPrepTime = null,
                minRating = null,
                difficulty = null,
                searchQuery = "",
                searchText = ""
            )
        }
        loadFeed()
    }

    /**
     * Saves a recipe directly to the user's Recipe Box with an implicit right swipe.
     *
     * @param recipe The recipe to bookmark.
     */
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

    /**
     * Clears the transient saved snackbar message once acknowledged.
     */
    fun consumeSavedMessage() {
        _state.update { it.copy(savedMessage = null) }
    }
}

//------------------------------End-Of-File------------------------------//
