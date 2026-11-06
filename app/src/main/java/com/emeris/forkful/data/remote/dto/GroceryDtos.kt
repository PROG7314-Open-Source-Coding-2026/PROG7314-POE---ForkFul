package com.emeris.forkful.data.remote.dto

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
