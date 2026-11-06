package com.emeris.forkful.ui.swipestack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.model.TagPoint
import com.emeris.forkful.domain.repository.DeckQuery
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Forkful - Tinder-Style Recipe Swipe Deck
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Observable UI state encapsulating the active card stack and interaction metrics.
 *
 * @property isLoading True when the deck is being fetched from the database or API.
 * @property errorMessage Descriptive error text if the swipe query encounters failure.
 * @property deck The list of recipes matching the selected mood shelf.
 * @property currentIndex Pointer to the top card currently awaiting gesture input.
 * @property moodKey Identifier of the active mood filter (e.g., "comfort", "vegan", "quick").
 * @property isSwiping Concurrency guard preventing multiple simultaneous swipe actions.
 * @property lastTagPoints Adaptive machine learning vector updates generated from user preference swipes.
 * @property sessionCount Total number of swipes completed in the active session.
 * @property reachedEnd True when all cards in the stack have been swiped.
 */
data class SwipeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val deck: List<Recipe> = emptyList(),
    val currentIndex: Int = 0,
    val moodKey: String = "comfort",
    val isSwiping: Boolean = false,
    val lastTagPoints: List<TagPoint> = emptyList(),
    val sessionCount: Int = 0,
    val reachedEnd: Boolean = false
) {
    /** The recipe card currently positioned at the top of the stack. */
    val currentRecipe: Recipe? get() = deck.getOrNull(currentIndex)

    /** Human-readable counter of remaining unswiped dishes. */
    val stackLabel: String get() = "${(deck.size - currentIndex).coerceAtLeast(0)} dishes in this stack"
}

/**
 * ViewModel managing gesture-driven recipe cards, mood-specific deck filtration,
 * optimistic stack advancement, and preference profile learning.
 *
 * @property recipeRepository Repository providing card retrieval, swipe recording, and preference syncing.
 */
class SwipeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SwipeUiState())
    val state: StateFlow<SwipeUiState> = _state.asStateFlow()

    /**
     * Loads a curated recipe deck for a given mood key, filtering out incompatible categories.
     *
     * @param moodKey The culinary mood or dietary filter key to populate.
     */
    fun loadDeck(moodKey: String) {
        _state.update { it.copy(moodKey = moodKey, isLoading = true, errorMessage = null, reachedEnd = false) }
        viewModelScope.launch {
            recipeRepository.getDeck(DeckQuery(mood = moodKey, limit = 50, excludeSwiped = true))
                .onSuccess { rawDeck ->
                    val target = moodKey.lowercase().trim()

                    // Ensure other cuisines do not leak into the stack
                    val curatedDeck = rawDeck.filter { recipe ->
                        val categoryMatches = recipe.category.lowercase().trim() == target
                        val tagMatches = recipe.tags.any { it.lowercase().contains(target) }
                        val specialMoodMatches = when (target) {
                            "vegan" -> recipe.dietaryTags.any { it.equals("vegan", ignoreCase = true) } ||
                                    recipe.tags.any { it.contains("vegan", ignoreCase = true) }
                            "healthy" -> recipe.category.equals("Healthy", ignoreCase = true) || recipe.calories <= 450
                            "comfort" -> recipe.category.equals("Comfort", ignoreCase = true) || recipe.category.equals("Braai", ignoreCase = true)
                            "pantry" -> true
                            else -> false
                        }
                        categoryMatches || tagMatches || specialMoodMatches
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            deck = curatedDeck,
                            currentIndex = 0,
                            reachedEnd = curatedDeck.isEmpty()
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load the stack")
                    }
                }
        }
    }

    /**
     * Records a swipe decision (Left: Dislike, Right: Like/Save, Up: Cook/Details),
     * optimistically advancing the stack and updating taste preference vectors.
     *
     * @param direction Swipe direction gesture detected by the UI.
     */
    fun swipe(direction: SwipeDirection) {
        val snapshot = _state.value
        val recipe = snapshot.currentRecipe ?: return
        if (snapshot.isSwiping) return

        // Optimistic advance: immediately increment card pointer
        _state.update {
            it.copy(
                isSwiping = true,
                currentIndex = it.currentIndex + 1,
                sessionCount = it.sessionCount + 1,
                reachedEnd = it.currentIndex + 1 >= it.deck.size
            )
        }

        viewModelScope.launch {
            recipeRepository.recordSwipe(recipe.id, direction, snapshot.moodKey)
                .onSuccess { outcome ->
                    _state.update {
                        it.copy(isSwiping = false, lastTagPoints = outcome.updatedTagPoints)
                    }
                }
                .onFailure { error ->
                    ForkfulLogger.logAction("SWIPE", "Sync failed: ${error.message}")
                    _state.update { it.copy(isSwiping = false) }
                }
        }
    }

    /**
     * Reloads the current mood deck from scratch.
     */
    fun reload() {
        loadDeck(_state.value.moodKey)
    }
}

//------------------------------End-Of-File------------------------------//
