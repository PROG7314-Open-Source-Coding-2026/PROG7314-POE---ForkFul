package com.emeris.forkful.domain.repository

/**
 * Forkful - Recipe Discovery Domain Repository Contract
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain interface declaring swipe deck retrieval, detail inspection, and bookmark management.
 */

import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.RecipeBoxFilter
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.model.SwipeOutcome

//Deck query model
data class DeckQuery(
    val mood: String? = null,
    val limit: Int = 20,
    val maxPrepTime: Int? = null,
    val minRating: Double? = null,
    val difficulty: String? = null,
    val search: String? = null,
    val excludeSwiped: Boolean = false
)

//Recipe repo contract
interface RecipeRepository {
    suspend fun getDeck(query: DeckQuery): Result<List<Recipe>>
    suspend fun getRecipeDetail(recipeId: String): Result<Recipe>
    suspend fun getRecipeBox(filter: RecipeBoxFilter): Result<List<Recipe>>
    suspend fun recordSwipe(recipeId: String, direction: SwipeDirection, moodProfileKey: String): Result<SwipeOutcome>
    suspend fun setCooked(recipeId: String, cooked: Boolean): Result<Boolean>
    suspend fun unsaveRecipe(recipeId: String): Result<Boolean>
}

//------------------------------End-Of-File------------------------------//
