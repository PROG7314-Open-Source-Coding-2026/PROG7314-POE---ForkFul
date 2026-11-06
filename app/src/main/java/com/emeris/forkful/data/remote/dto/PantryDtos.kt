package com.emeris.forkful.data.remote.dto

/**
 * Forkful - Pantry Network Data Transfer Objects (DTOs)
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Data transfer schemas for pantry items, inventory additions, expiry dates, and recipe match scoring.
 */

import kotlinx.serialization.Serializable

//Pantry DTOs
@Serializable
data class PantryItemDto(
    val pantryItemId: String,
    val name: String,
    @Serializable(with = StringOrNumericSerializer::class)
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

//Edit pantry item req (null fields are left unchanged)
@Serializable
data class UpdatePantryItemRequest(
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

//------------------------------End-Of-File------------------------------//
