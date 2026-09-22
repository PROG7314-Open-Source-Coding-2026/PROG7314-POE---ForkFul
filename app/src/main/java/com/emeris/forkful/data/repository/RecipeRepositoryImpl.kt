package com.emeris.forkful.data.repository

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.data.mapper.toOutcome
import com.emeris.forkful.data.remote.dto.MarkCookedRequest
import com.emeris.forkful.data.remote.dto.SwipeRequest
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.RecipeBoxFilter
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.model.SwipeOutcome
import com.emeris.forkful.domain.repository.DeckQuery
import com.emeris.forkful.domain.repository.RecipeRepository

//Recipe repo impl
class RecipeRepositoryImpl(
    private val api: ForkfulApi
) : RecipeRepository {

    override suspend fun getDeck(query: DeckQuery): Result<List<Recipe>> = runCatching {
        ForkfulLogger.logNetwork(
            "recipes-deck",
            "mood=${query.mood ?: "-"} limit=${query.limit} prep<=${query.maxPrepTime ?: "-"}"
        )
        api.recipesDeck(
            mood = query.mood,
            limit = query.limit,
            maxPrepTime = query.maxPrepTime,
            minRating = query.minRating,
            difficulty = query.difficulty,
            search = query.search?.takeIf { it.isNotBlank() }
        ).map { it.toDomain() }
    }

    override suspend fun getRecipeDetail(recipeId: String): Result<Recipe> = runCatching {
        ForkfulLogger.logNetwork("recipe-detail", "recipeId=$recipeId")
        api.recipeDetail(recipeId).toDomain()
    }

    override suspend fun getRecipeBox(filter: RecipeBoxFilter): Result<List<Recipe>> = runCatching {
        ForkfulLogger.logNetwork("recipe-box", "status=${filter.wireName}")
        api.recipeBox(filter.wireName).map { it.toDomain() }
    }

    override suspend fun recordSwipe(
        recipeId: String,
        direction: SwipeDirection,
        moodProfileKey: String
    ): Result<SwipeOutcome> = runCatching {
        ForkfulLogger.logAction("SWIPE", "$direction on $recipeId (mood=$moodProfileKey)")
        api.recordSwipe(
            SwipeRequest(
                recipeId = recipeId,
                moodProfileId = moodProfileKey,
                direction = direction.wireName
            )
        ).toOutcome()
    }

    override suspend fun setCooked(recipeId: String, cooked: Boolean): Result<Boolean> = runCatching {
        ForkfulLogger.logAction("RECIPE_BOX", "mark cooked=$cooked for $recipeId")
        api.markCooked(MarkCookedRequest(recipeId, cooked))
        true
    }
}
