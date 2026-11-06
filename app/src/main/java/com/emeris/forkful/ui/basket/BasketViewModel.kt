package com.emeris.forkful.ui.basket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.domain.model.GroceryAisle
import com.emeris.forkful.domain.repository.GroceryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Basket UI state
data class BasketUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val aisles: List<GroceryAisle> = emptyList(),
    val pendingItemId: String? = null,
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    val bannerText: String? = "Items are added automatically whenever you save a recipe - anything already in your pantry is subtracted. Tap + to add your own."
) {
    val totalItemCount: Int get() = aisles.sumOf { it.items.size }
    val checkedItemCount: Int get() = aisles.sumOf { aisle -> aisle.items.count { it.isChecked } }

    fun asShareText(): String = buildString {
        appendLine("My Forkful basket")
        aisles.forEach { aisle ->
            appendLine()
            appendLine(aisle.name.uppercase())
            aisle.items.forEach { item ->
                appendLine("- [${if (item.isChecked) "x" else " "}] ${item.name} (${item.quantity})")
            }
        }
    }
}

class BasketViewModel(
    private val groceryRepository: GroceryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BasketUiState())
    val state: StateFlow<BasketUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            groceryRepository.getBasket()
                .onSuccess { aisles ->
                    _state.update { it.copy(isLoading = false, aisles = aisles) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Could not load your basket")
                    }
                }
        }
    }

    //Quiet reload after a change, keeps the list on screen
    private fun refresh() {
        viewModelScope.launch {
            groceryRepository.getBasket().onSuccess { aisles ->
                _state.update { it.copy(aisles = aisles) }
            }
        }
    }

    fun setChecked(itemId: String, isChecked: Boolean) {
        _state.update { current ->
            current.copy(
                pendingItemId = itemId,
                aisles = current.aisles.map { aisle ->
                    aisle.copy(
                        items = aisle.items.map { item ->
                            if (item.id == itemId) item.copy(isChecked = isChecked) else item
                        }
                    )
                }
            )
        }
        viewModelScope.launch {
            groceryRepository.setChecked(itemId, isChecked)
                .onSuccess {
                    ForkfulLogger.logAction("BASKET", "Synced $itemId -> $isChecked")
                }
                .onFailure { error ->
                    ForkfulLogger.logAction("BASKET", "Toggle sync failed: ${error.message}")
                }
            _state.update { it.copy(pendingItemId = null) }
        }
    }

    //Remove one item (optimistic, restored on failure)
    fun removeItem(itemId: String) {
        val before = _state.value.aisles
        val removed = before.flatMap { it.items }.firstOrNull { it.id == itemId } ?: return
        _state.update { current ->
            current.copy(aisles = current.aisles.withoutItems { it.id == itemId })
        }
        viewModelScope.launch {
            groceryRepository.removeItem(itemId)
                .onSuccess {
                    _state.update { it.copy(snackbarMessage = "${removed.name} removed") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(aisles = before, snackbarMessage = error.message ?: "Could not remove ${removed.name}")
                    }
                }
        }
    }

    //Manual add from the Add item dialog
    fun addItem(name: String, quantity: String, onAdded: () -> Unit = {}) {
        if (_state.value.isSubmitting) return
        if (name.isBlank()) {
            _state.update { it.copy(snackbarMessage = "Please enter an item name") }
            return
        }
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            groceryRepository.addItem(name.trim(), quantity.trim().ifBlank { null })
                .onSuccess {
                    _state.update {
                        it.copy(isSubmitting = false, snackbarMessage = "${name.trim()} added to your basket")
                    }
                    onAdded()
                    refresh()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isSubmitting = false, snackbarMessage = error.message ?: "Could not add ${name.trim()}")
                    }
                }
        }
    }

    fun clearCollected() = clear(checkedOnly = true)

    fun clearAll() = clear(checkedOnly = false)

    private fun clear(checkedOnly: Boolean) {
        val before = _state.value.aisles
        if (checkedOnly && _state.value.checkedItemCount == 0) return
        _state.update { current ->
            current.copy(
                aisles = if (checkedOnly) current.aisles.withoutItems { it.isChecked } else emptyList()
            )
        }
        viewModelScope.launch {
            groceryRepository.clearItems(checkedOnly)
                .onSuccess {
                    _state.update {
                        it.copy(snackbarMessage = if (checkedOnly) "Collected items removed" else "Basket cleared")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(aisles = before, snackbarMessage = error.message ?: "Could not update your basket")
                    }
                }
        }
    }

    fun consumeSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    fun dismissBanner() {
        _state.update { it.copy(bannerText = null) }
    }

    private fun List<GroceryAisle>.withoutItems(
        predicate: (com.emeris.forkful.domain.model.GroceryItem) -> Boolean
    ): List<GroceryAisle> = map { aisle -> aisle.copy(items = aisle.items.filterNot(predicate)) }
        .filter { it.items.isNotEmpty() }
}
