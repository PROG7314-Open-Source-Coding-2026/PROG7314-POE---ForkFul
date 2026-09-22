package com.emeris.forkful.core.network

import com.emeris.forkful.data.remote.dto.AddPantryItemRequest
import com.emeris.forkful.data.remote.dto.AddPantryItemResponse
import com.emeris.forkful.data.remote.dto.AuthResponse
import com.emeris.forkful.data.remote.dto.GroceryAisleDto
import com.emeris.forkful.data.remote.dto.MarkCookedRequest
import com.emeris.forkful.data.remote.dto.PantryItemDto
import com.emeris.forkful.data.remote.dto.PantryMatchDto
import com.emeris.forkful.data.remote.dto.PantryMatchRequest
import com.emeris.forkful.data.remote.dto.RecipeDetailDto
import com.emeris.forkful.data.remote.dto.RecipeDeckItemDto
import com.emeris.forkful.data.remote.dto.SsoAuthRequest
import com.emeris.forkful.data.remote.dto.StatusResponse
import com.emeris.forkful.data.remote.dto.SwipeRequest
import com.emeris.forkful.data.remote.dto.SwipeResponse
import com.emeris.forkful.data.remote.dto.ToggleGroceryItemRequest
import com.emeris.forkful.data.remote.dto.UpdatePreferencesRequest
import com.emeris.forkful.data.remote.dto.UserPreferencesDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Typed REST client for the custom Forkful API implemented as Supabase
 * Edge Functions (Planning & Design document, section 5.2 - Endpoint
 * Specification).
 *
 * All endpoints except [authSso] require a Bearer session token which is
 * attached automatically by [AuthInterceptor].
 */
interface ForkfulApi {

    // -------------------------------------------------------------------------
    // Authentication (FR-01)
    // -------------------------------------------------------------------------

    @POST("functions/v1/auth-sso")
    suspend fun authSso(@Body body: SsoAuthRequest): AuthResponse

    // -------------------------------------------------------------------------
    // Discovery / swipe engine (FR-06 .. FR-12)
    // -------------------------------------------------------------------------

    @GET("functions/v1/recipes-deck")
    suspend fun recipesDeck(
        @Query("mood") mood: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("dietaryTags") dietaryTags: String? = null,
        @Query("maxPrepTime") maxPrepTime: Int? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("search") search: String? = null
    ): List<RecipeDeckItemDto>

    @GET("functions/v1/recipe-detail")
    suspend fun recipeDetail(@Query("recipeId") recipeId: String): RecipeDetailDto

    @POST("functions/v1/swipes")
    suspend fun recordSwipe(@Body body: SwipeRequest): SwipeResponse

    // -------------------------------------------------------------------------
    // Recipe Box (FR-15, FR-16)
    // -------------------------------------------------------------------------

    @GET("functions/v1/recipe-box")
    suspend fun recipeBox(@Query("status") status: String? = null): List<RecipeDeckItemDto>

    @POST("functions/v1/recipe-box")
    suspend fun markCooked(@Body body: MarkCookedRequest): StatusResponse

    // -------------------------------------------------------------------------
    // Pantry / Capture Fridge (FR-17 .. FR-20)
    // -------------------------------------------------------------------------

    @GET("functions/v1/pantry")
    suspend fun pantry(): List<PantryItemDto>

    @POST("functions/v1/pantry")
    suspend fun addPantryItem(@Body body: AddPantryItemRequest): AddPantryItemResponse

    @DELETE("functions/v1/pantry")
    suspend fun deletePantryItem(@Query("pantryItemId") pantryItemId: String): StatusResponse

    @POST("functions/v1/pantry-match")
    suspend fun pantryMatch(@Body body: PantryMatchRequest): List<PantryMatchDto>

    // -------------------------------------------------------------------------
    // Basket / Smart Grocery Aggregator (FR-21 .. FR-24)
    // -------------------------------------------------------------------------

    @GET("functions/v1/groceries")
    suspend fun groceries(): List<GroceryAisleDto>

    @PUT("functions/v1/groceries-item")
    suspend fun toggleGroceryItem(
        @Query("itemId") itemId: String,
        @Body body: ToggleGroceryItemRequest
    ): StatusResponse

    // -------------------------------------------------------------------------
    // Settings / preferences (FR-25 .. FR-27)
    // -------------------------------------------------------------------------

    @GET("functions/v1/user-preferences")
    suspend fun userPreferences(): UserPreferencesDto

    @PUT("functions/v1/user-preferences")
    suspend fun updateUserPreferences(@Body body: UpdatePreferencesRequest): StatusResponse
}
