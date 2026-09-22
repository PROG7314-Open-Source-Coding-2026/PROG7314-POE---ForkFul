package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

//Pantry DTOs
@Serializable
data class PantryItemDto(
    val pantryItemId: String,
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val expiryDate: String? = null,
    val category: String? = null
)

//Add pantry item req
@Serializable
data class AddPantryItemRequest(
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val expiryDate: String? = null
)

//Add pantry item res
@Serializable
data class AddPantryItemResponse(
    val pantryItemId: String,
    val status: String
)

//Pantry match req
@Serializable
data class PantryMatchRequest(
    val missingThreshold: Int = 2
)

//Pantry match dto
@Serializable
data class PantryMatchDto(
    val recipe: RecipeDeckItemDto,
    val matchPercentage: Int,
    val missingIngredients: List<IngredientDto> = emptyList()
)
