package com.emeris.forkful.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.util.PantryUtils
import com.emeris.forkful.data.local.LocalPantryRepository
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch
import com.emeris.forkful.domain.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Immutable state model for the Pantry screen.
 * Captures network loading states, offline synchronization, active pantry items,
 * and computed recipe matches.
 */
data class PantryUiState(
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val items: List<PantryItem> = emptyList(),
    val matches: List<PantryMatch> = emptyList(),
    val addSuccessMessage: String? = null,
    val isOffline: Boolean = false,
    val lastSynced: String? = null
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

/**
 * ViewModel managing pantry inventory operations.
 * Implements an offline-first architecture by observing the Room database [LocalPantryRepository],
 * synchronizing live updates with the remote Supabase API [PantryRepository], and computing
 * pantry-matched recipes.
 */
class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val localPantryRepository: LocalPantryRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(PantryUiState())
    val state: StateFlow<PantryUiState> = _state.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @Suppress("unused")
    val quickAddSuggestions = listOf(
        "Tomatoes", "Onions", "Garlic", "Eggs", "Milk", "Butter", "Cheese",
        "Chicken", "Rice", "Pasta", "Spinach", "Potatoes", "Basil", "Olive Oil"
    )

    init {
        observeLocalPantry()
        loadPantry()
    }

    private fun observeLocalPantry() {
        val localRepo = localPantryRepository ?: return
        viewModelScope.launch {
            localRepo.getAll().collect { localItems ->
                _state.update { current ->
                    if (current.items.isEmpty() || current.isOffline) {
                        current.copy(items = localItems, isLoading = false)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun loadPantry() {
        _state.update { it.copy(isLoading = true, errorMessage = null, isOffline = false) }
        viewModelScope.launch {
            pantryRepository.getPantry()
                .onSuccess { items ->
                    val now = LocalDateTime.now().format(timeFormatter)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            isOffline = false,
                            lastSynced = now
                        )
                    }
                    // Sync any offline changes now that we are online
                    syncPendingItems()
                    loadMatches()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isOffline = true,
                            errorMessage = null
                        )
                    }
                    ForkfulLogger.logAction("PANTRY", "Using local data (offline): ${error.message}")
                }
        }
    }

    // Send any items that were added offline to the server
    private fun syncPendingItems() {
        val localRepo = localPantryRepository ?: return
        viewModelScope.launch {
            val pending = localRepo.getPendingSync()
            if (pending.isEmpty()) return@launch

            ForkfulLogger.logAction("PANTRY", "Syncing ${pending.size} offline items")
            pending.forEach { entity ->
                pantryRepository.addItem(
                    name = entity.name,
                    quantity = null,
                    unit = null,
                    expiryDate = null
                ).onSuccess {
                    localRepo.markSynced(entity.id)
                }.onFailure { error ->
                    ForkfulLogger.logAction("PANTRY", "Failed to sync ${entity.name}: ${error.message}")
                }
            }
            // Refresh from server after sync
            pantryRepository.getPantry().onSuccess { items ->
                val now = LocalDateTime.now().format(timeFormatter)
                _state.update { it.copy(items = items, lastSynced = now, isOffline = false) }
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
                    _state.update {
                        it.copy(isUpdating = false, addSuccessMessage = error.message ?: "Could not update item")
                    }
                }
        }
    }

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
                    localPantryRepository?.add(item.name, item.category)
                    _state.update { current ->
                        current.copy(
                            isAdding = false,
                            items = current.items + item,
                            addSuccessMessage = "${item.name} added to your pantry"
                        )
                    }
                    loadMatches()
                }
                .onFailure {
                    localPantryRepository?.add(name.trim(), "Other")
                    _state.update { current ->
                        current.copy(
                            isAdding = false,
                            isOffline = true,
                            addSuccessMessage = "$name saved locally (will sync later)"
                        )
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
                    localPantryRepository?.remove(pantryItemId)
                    _state.update { current ->
                        current.copy(items = current.items.filterNot { it.id == pantryItemId })
                    }
                    loadMatches()
                }
                .onFailure {
                    localPantryRepository?.remove(pantryItemId)
                    _state.update { current ->
                        current.copy(
                            items = current.items.filterNot { it.id == pantryItemId },
                            isOffline = true
                        )
                    }
                }
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(addSuccessMessage = null, errorMessage = null) }
    }
}

//------------------------------End-Of-File------------------------------//
