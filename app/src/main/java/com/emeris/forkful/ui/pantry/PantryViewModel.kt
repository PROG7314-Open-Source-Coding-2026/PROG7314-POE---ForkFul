package com.emeris.forkful.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.util.PantryUtils
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch
import com.emeris.forkful.domain.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Pantry UI state
data class PantryUiState(
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val errorMessage: String? = null,
    val items: List<PantryItem> = emptyList(),
    val matches: List<PantryMatch> = emptyList(),
    val addSuccessMessage: String? = null
) {
    val expiringSoon: List<PantryItem>
        get() = items.filter { PantryUtils.isExpiringSoon(it.daysUntilExpiry) || PantryUtils.isExpired(it.daysUntilExpiry) }
    val bestMatch: PantryMatch? get() = matches.maxByOrNull { it.matchPercentage }
}

class PantryViewModel(
    private val pantryRepository: PantryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PantryUiState())
    val state: StateFlow<PantryUiState> = _state.asStateFlow()

    val quickAddSuggestions = listOf(
        "Tomatoes", "Onions", "Garlic", "Eggs", "Milk", "Butter", "Cheese",
        "Chicken", "Rice", "Pasta", "Spinach", "Potatoes", "Basil", "Olive Oil"
    )

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            pantryRepository.getPantry()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
                    loadMatches()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load your pantry")
                    }
                }
        }
    }

    private fun loadMatches() {
        viewModelScope.launch {
            pantryRepository.getMatches(missingThreshold = 2)
                .onSuccess { matches ->
                    _state.update { it.copy(matches = matches.take(3)) }
                }
                .onFailure { error ->
                    ForkfulLogger.logAction("PANTRY", "Match refresh failed: ${error.message}")
                }
        }
    }

    fun addItem(name: String, quantity: String?, unit: String?, expiryDate: String?) {
        if (_state.value.isAdding) return
        _state.update { it.copy(isAdding = true, errorMessage = null) }
        viewModelScope.launch {
            pantryRepository.addItem(name, quantity, unit, expiryDate)
                .onSuccess { item ->
                    _state.update { current ->
                        current.copy(
                            isAdding = false,
                            items = current.items + item,
                            addSuccessMessage = "${item.name} added to your pantry"
                        )
                    }
                    loadMatches()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isAdding = false, errorMessage = error.message ?: "Could not add item")
                    }
                }
        }
    }

    fun quickAdd(name: String) = addItem(name, quantity = null, unit = null, expiryDate = null)

    fun removeItem(pantryItemId: String) {
        viewModelScope.launch {
            pantryRepository.removeItem(pantryItemId)
                .onSuccess {
                    _state.update { current ->
                        current.copy(items = current.items.filterNot { it.id == pantryItemId })
                    }
                    loadMatches()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(errorMessage = error.message ?: "Could not remove item")
                    }
                }
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(addSuccessMessage = null, errorMessage = null) }
    }
}
