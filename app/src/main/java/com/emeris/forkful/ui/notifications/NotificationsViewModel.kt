package com.emeris.forkful.ui.notifications

/**
 * Forkful - In-App Notification Center ViewModel
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Manages notification list retrieval, mark-as-read actions, and clearing notifications.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.util.PantryUtils
import com.emeris.forkful.domain.model.NotificationModel
import com.emeris.forkful.domain.model.NotificationType
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.repository.DeckQuery
import com.emeris.forkful.domain.repository.PantryRepository
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val pantryItems: List<PantryItem> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val notifications: List<NotificationModel> = emptyList(),
    val errorMessage: String? = null
)

class NotificationsViewModel(
    private val pantryRepository: PantryRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val pantryResult = pantryRepository.getPantry()
            val deckResult = recipeRepository.getDeck(DeckQuery(limit = 15))

            val pantry = pantryResult.getOrDefault(emptyList())
            val recipes = deckResult.getOrDefault(emptyList())

            val generatedAlerts = buildList {
                // 1. Check real pantry items expiring soon
                val expiringItems = pantry.filter { item ->
                    item.daysUntilExpiry?.let { PantryUtils.isExpiringSoon(it) || PantryUtils.isExpired(it) } ?: false
                }

                expiringItems.forEachIndexed { index, item ->
                    val days = item.daysUntilExpiry ?: 0
                    add(
                        NotificationModel(
                            id = "expiry_${item.id}_$index",
                            title = "Action Required: ${item.name} Expiring Soon",
                            description = "Your ${item.name} in your pantry ${if (days <= 0) "expires today!" else "expires in $days day(s)."}. Tap to find recipes to use it up before it spoils.",
                            timeAgo = "Live",
                            type = NotificationType.ACTION_REQUIRED,
                            isUnread = true
                        )
                    )
                }

                // 2. Real meal recommendation based on time of day and live database recipes
                val currentHour = LocalTime.now().hour
                val mealType = when {
                    currentHour < 11 -> "Breakfast"
                    currentHour < 15 -> "Lunch"
                    else -> "Dinner"
                }

                val matchedRecipe = recipes.maxByOrNull { it.matchPercentage } ?: recipes.firstOrNull()
                if (matchedRecipe != null) {
                    add(
                        NotificationModel(
                            id = "meal_rec_${matchedRecipe.id}",
                            title = "$mealType Match: ${matchedRecipe.title}",
                            description = "${matchedRecipe.description} Ready in ${matchedRecipe.prepTimeMinutes} min with a ${matchedRecipe.matchPercentage}% pantry match.",
                            timeAgo = "1h ago",
                            type = NotificationType.PANTRY_MATCH,
                            isUnread = true
                        )
                    )
                }

                // 3. Prep reminder for any saved recipes
                val savedRecipe = recipes.firstOrNull { it.isSaved } ?: recipes.getOrNull(1)
                if (savedRecipe != null) {
                    add(
                        NotificationModel(
                            id = "prep_${savedRecipe.id}",
                            title = "Prep Reminder: ${savedRecipe.title}",
                            description = "Don't forget to take ingredients out of the fridge or freezer for tonight's ${savedRecipe.category} dish.",
                            timeAgo = "3h ago",
                            type = NotificationType.PREP_REMINDER,
                            isUnread = false
                        )
                    )
                }

                // 4. Live information if user's pantry is completely empty
                if (pantry.isEmpty()) {
                    add(
                        NotificationModel(
                            id = "empty_pantry_hint",
                            title = "Pantry Radar Active",
                            description = "Add ingredients to your pantry in the Capture Fridge tab to receive automatic expiration warnings and zero-waste suggestions.",
                            timeAgo = "Just now",
                            type = NotificationType.PANTRY_MATCH,
                            isUnread = true
                        )
                    )
                }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    pantryItems = pantry,
                    recipes = recipes,
                    notifications = generatedAlerts
                )
            }
            ForkfulLogger.logAction("NOTIFICATIONS", "Generated ${generatedAlerts.size} live alerts from DB")
        }
    }
}

//------------------------------End-Of-File------------------------------//
