package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response for GET /functions/v1/groceries — items grouped by retail aisle:
 * `[ { aisle, items: [ { name, quantity, unit, isChecked } ] } ]`.
 */
@Serializable
data class GroceryAisleDto(
    val aisle: String,
    val items: List<GroceryItemDto> = emptyList()
)

@Serializable
data class GroceryItemDto(
    val itemId: String,
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val isChecked: Boolean = false
)

/** Request body for PUT /functions/v1/groceries-item: `{ isChecked }`. */
@Serializable
data class ToggleGroceryItemRequest(
    val isChecked: Boolean
)

/** Generic `{ status }` response. */
@Serializable
data class StatusResponse(
    val status: String
)

/** Request body for POST /functions/v1/recipe-box (mark as cooked). */
@Serializable
data class MarkCookedRequest(
    val recipeId: String,
    val cooked: Boolean = true
)
