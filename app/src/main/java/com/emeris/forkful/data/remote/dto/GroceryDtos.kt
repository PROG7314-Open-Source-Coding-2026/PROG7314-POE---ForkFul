package com.emeris.forkful.data.remote.dto

/**
 * Forkful - Grocery Network Data Transfer Objects (DTOs)
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Data transfer schemas for grocery basket requests, aisle mappings, item toggles, and deletion calls.
 */

import kotlinx.serialization.Serializable

//Grocery DTOs
@Serializable
data class GroceryAisleDto(
    val aisle: String,
    val items: List<GroceryItemDto> = emptyList()
)

@Serializable
data class GroceryItemDto(
    val itemId: String,
    val name: String,
    @Serializable(with = StringOrNumericSerializer::class)
    val quantity: String? = null,
    val unit: String? = null,
    val isChecked: Boolean = false
)

//Toggle grocery item req
@Serializable
data class ToggleGroceryItemRequest(
    val isChecked: Boolean
)

//Manual basket item req
@Serializable
data class AddGroceryItemRequest(
    val name: String,
    val quantity: String? = null,
    val unit: String? = null
)

//Manual basket item res
@Serializable
data class AddGroceryItemResponse(
    val itemId: String,
    val aisle: String? = null,
    val status: String
)

//Status res
@Serializable
data class StatusResponse(
    val status: String
)

//Mark cooked req
@Serializable
data class MarkCookedRequest(
    val recipeId: String,
    val cooked: Boolean = true
)

//------------------------------End-Of-File------------------------------//
