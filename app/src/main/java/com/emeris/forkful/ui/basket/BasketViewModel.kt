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

/**
 * Forkful - Smart Grocery & Basket Planning
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Represents the observable UI state of the smart grocery basket.
 *
 * @property isLoading True when the grocery repository is fetching the active basket items.
 * @property errorMessage Error text displayed if network or database operations fail.
 * @property aisles The list of grocery items grouped by supermarket aisle/category.
 * @property pendingItemId ID of an item currently undergoing optimistic checkbox toggling.
 * @property isSubmitting True when an item is currently being inserted via bottom sheet.
 * @property snackbarMessage Transient toast/snackbar message presented to the user.
 * @property bannerText Contextual educational banner text explaining automated basket calculation.
 */
data class BasketUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val aisles: List<GroceryAisle> = emptyList(),
    val pendingItemId: String? = null,
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    val bannerText: String? = "Items are added automatically whenever you save a recipe - anything already in your pantry is subtracted. Tap + to add your own."
) {
    /** Total number of grocery items across all aisles. */
    val totalItemCount: Int get() = aisles.sumOf { it.items.size }

    /** Number of grocery items marked as collected/checked. */
    val checkedItemCount: Int get() = aisles.sumOf { aisle -> aisle.items.count { it.isChecked } }

    /**
     * Converts the current basket contents into a cleanly formatted plaintext list
     * suitable for sharing via WhatsApp, SMS, or email.
     */
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

/**
 * ViewModel managing grocery basket items, optimistic UI updates, aisle grouping,
 * pantry deduction feedback, and real-time backend synchronization.
 *
 * @property groceryRepository Repository providing CRUD operations for grocery items and sync.
 */
class BasketViewModel(
    private val groceryRepository: GroceryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BasketUiState())
    val state: StateFlow<BasketUiState> = _state.asStateFlow()

    init {
        load()
    }

    /**
     * Loads the full grocery basket from the repository and groups items into aisles.
     */
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

    /**
     * Background refresh that updates aisles in-place without triggering full screen loading spinners.
     */
    private fun refresh() {
        viewModelScope.launch {
            groceryRepository.getBasket().onSuccess { aisles ->
                _state.update { it.copy(aisles = aisles) }
            }
        }
    }

    /**
     * Optimistically toggles the checked status of a grocery item and persists to repository.
     *
     * @param itemId Unique ID of the grocery item.
     * @param isChecked New checked state.
     */
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

    /**
     * Optimistically deletes an item from the basket, rolling back if the network call fails.
     *
     * @param itemId Unique ID of the item to delete.
     */
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

    /**
     * Adds a custom grocery item directly into the basket.
     *
     * @param name Descriptive ingredient name.
     * @param quantity Amount or packaging string (e.g., "500g", "2 cans").
     * @param onAdded Optional callback invoked upon successful addition.
     */
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

    /**
     * Clears only the items marked as checked/purchased.
     */
    fun clearCollected() = clear(checkedOnly = true)

    /**
     * Empties the entire grocery basket.
     */
    fun clearAll() = clear(checkedOnly = false)

    /**
     * Executes bulk deletion of items from the basket with optimistic rollback.
     */
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

    /**
     * Consumes transient snackbar text after user acknowledgement.
     */
    fun consumeSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    /**
     * Dismisses the informational banner card.
     */
    fun dismissBanner() {
        _state.update { it.copy(bannerText = null) }
    }

    private fun List<GroceryAisle>.withoutItems(
        predicate: (com.emeris.forkful.domain.model.GroceryItem) -> Boolean
    ): List<GroceryAisle> = map { aisle -> aisle.copy(items = aisle.items.filterNot(predicate)) }
        .filter { it.items.isNotEmpty() }
}

//------------------------------End-Of-File------------------------------//
