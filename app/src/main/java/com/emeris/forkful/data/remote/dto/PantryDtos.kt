package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response row for GET /functions/v1/pantry:
 * `{ pantryItemId, name, quantity, unit, expiryDate }`.
 */
@Serializable
data class PantryItemDto(
    val pantryItemId: String,
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val expiryDate: String? = null,
    val category: String? = null
)

/** Request body for POST /functions/v1/pantry. */
@Serializable
data class AddPantryItemRequest(
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val expiryDate: String? = null
)

/** Response body for POST /functions/v1/pantry: `{ pantryItemId, status }`. */
@Serializable
data class AddPantryItemResponse(
    val pantryItemId: String,
    val status: String
)

/** Request body for POST /functions/v1/pantry-match. */
@Serializable
data class PantryMatchRequest(
    val missingThreshold: Int = 2
)

/** One pantry-match result row. */
@Serializable
data class PantryMatchDto(
    val recipe: RecipeDeckItemDto,
    val matchPercentage: Int,
    val missingIngredients: List<IngredientDto> = emptyList()
)
