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

/** Basket state (FR-21 .. FR-24). */
data class BasketUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val aisles: List<GroceryAisle> = emptyList(),
    val pendingItemId: String? = null,
    val bannerText: String? = "Items are added automatically whenever you save a recipe - anything already in your pantry is subtracted."
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

    fun dismissBanner() {
        _state.update { it.copy(bannerText = null) }
    }
}
