package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

//Swipe req
@Serializable
data class SwipeRequest(
    val recipeId: String,
    val moodProfileId: String,
    val direction: String
)

//Tag point DTO
@Serializable
data class TagPointDto(
    val moodProfileKey: String,
    val tagName: String,
    val points: Int
)

//Swipe res
@Serializable
data class SwipeResponse(
    val status: String,
    val swipeId: String,
    val updatedTagPoints: List<TagPointDto> = emptyList()
)
