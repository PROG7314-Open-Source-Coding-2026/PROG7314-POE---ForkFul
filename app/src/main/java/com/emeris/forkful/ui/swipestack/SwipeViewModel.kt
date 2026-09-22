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

//Swipe deck state
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
    val currentRecipe: Recipe? get() = deck.getOrNull(currentIndex)
    val stackLabel: String get() = "${deck.size - currentIndex} dishes in this stack"
}

//Swipe ViewModel
class SwipeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SwipeUiState())
    val state: StateFlow<SwipeUiState> = _state.asStateFlow()

    fun loadDeck(moodKey: String) {
        _state.update { it.copy(moodKey = moodKey, isLoading = true, errorMessage = null, reachedEnd = false) }
        viewModelScope.launch {
            recipeRepository.getDeck(DeckQuery(mood = moodKey, limit = 20))
                .onSuccess { deck ->
                    _state.update {
                        it.copy(isLoading = false, deck = deck, currentIndex = 0)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load the stack")
                    }
                }
        }
    }

    //Record swipe
    fun swipe(direction: SwipeDirection) {
        val snapshot = _state.value
        val recipe = snapshot.currentRecipe ?: return
        if (snapshot.isSwiping) return

        //Optimistic advance
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

    fun reload() {
        loadDeck(_state.value.moodKey)
    }
}
