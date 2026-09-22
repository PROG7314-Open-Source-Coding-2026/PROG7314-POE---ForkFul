package com.emeris.forkful.domain.repository

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
    val search: String? = null
)

//Recipe repo contract
interface RecipeRepository {
    suspend fun getDeck(query: DeckQuery): Result<List<Recipe>>
    suspend fun getRecipeDetail(recipeId: String): Result<Recipe>
    suspend fun getRecipeBox(filter: RecipeBoxFilter): Result<List<Recipe>>
    suspend fun recordSwipe(recipeId: String, direction: SwipeDirection, moodProfileKey: String): Result<SwipeOutcome>
    suspend fun setCooked(recipeId: String, cooked: Boolean): Result<Boolean>
}
