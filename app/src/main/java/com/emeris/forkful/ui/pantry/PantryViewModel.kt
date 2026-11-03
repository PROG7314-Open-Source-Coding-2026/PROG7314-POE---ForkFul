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
import java.time.LocalDate

data class PantryUiState(
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val items: List<PantryItem> = emptyList(),
    val matches: List<PantryMatch> = emptyList(),
    val addSuccessMessage: String? = null
) {
    @Suppress("unused")
    val expiringSoon: List<PantryItem>
        get() = items.filter { item ->
            item.daysUntilExpiry?.let { days ->
                PantryUtils.isExpiringSoon(days) || PantryUtils.isExpired(days)
            } ?: false
        }

    @Suppress("unused")
    val bestMatch: PantryMatch? get() = matches.maxByOrNull { it.matchPercentage }
}

class PantryViewModel(
    private val pantryRepository: PantryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PantryUiState())
    val state: StateFlow<PantryUiState> = _state.asStateFlow()

    @Suppress("unused")
    val quickAddSuggestions = listOf(
        "Tomatoes", "Onions", "Garlic", "Eggs", "Milk", "Butter", "Cheese",
        "Chicken", "Rice", "Pasta", "Spinach", "Potatoes", "Basil", "Olive Oil"
    )

    init {
        loadPantry()
    }

    fun loadPantry() {
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

    fun addPantryItem(name: String, quantity: String, category: String, daysUntilExpiry: Int) {
        val expiryDate = LocalDate.now().plusDays(daysUntilExpiry.toLong()).toString()
        val (qty, unit) = splitQuantity(quantity)
        addItem(name = name, quantity = qty, unit = unit, expiryDate = expiryDate)
    }

    //Edit: daysUntilExpiry null keeps the current expiry date
    fun updatePantryItem(item: PantryItem, name: String, quantity: String, daysUntilExpiry: Int?) {
        if (_state.value.isUpdating) return
        val (qty, unit) = splitQuantity(quantity)
        val expiryDate = daysUntilExpiry?.let { LocalDate.now().plusDays(it.toLong()).toString() }
            ?: item.expiryDate
        _state.update { it.copy(isUpdating = true, errorMessage = null) }
        viewModelScope.launch {
            pantryRepository.updateItem(item.id, name, qty, unit, expiryDate)
                .onSuccess {
                    ForkfulLogger.logAction("PANTRY", "Updated ${item.name} -> $name")
                    _state.update { current ->
                        current.copy(
                            isUpdating = false,
                            items = current.items.map { existing ->
                                if (existing.id != item.id) existing else existing.copy(
                                    name = name.trim(),
                                    quantity = qty,
                                    unit = unit,
                                    expiryDate = expiryDate,
                                    daysUntilExpiry = PantryUtils.daysUntilExpiry(expiryDate)
                                )
                            },
                            addSuccessMessage = "${name.trim()} updated"
                        )
                    }
                    loadMatches()
                }
                .onFailure { error ->
                    //Snackbar only - the pantry list stays on screen
                    _state.update {
                        it.copy(isUpdating = false, addSuccessMessage = error.message ?: "Could not update item")
                    }
                }
        }
    }

    //"500 g" -> ("500", "g"); "a bunch" -> (null, "a bunch")
    private fun splitQuantity(quantity: String): Pair<String?, String?> {
        val parts = quantity.trim().split(Regex("\\s+"), limit = 2).filter { it.isNotBlank() }
        val first = parts.getOrNull(0) ?: return null to null
        return if (first.replace(",", ".").toDoubleOrNull() != null) {
            first to parts.getOrNull(1)
        } else {
            null to quantity.trim()
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

    @Suppress("unused")
    fun quickAdd(name: String) = addItem(name, quantity = null, unit = null, expiryDate = null)

    fun removePantryItem(pantryItemId: String) {
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