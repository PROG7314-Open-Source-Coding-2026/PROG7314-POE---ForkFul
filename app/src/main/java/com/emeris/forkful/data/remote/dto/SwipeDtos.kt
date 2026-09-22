package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request body for POST /functions/v1/swipes:
 * `{ recipeId, moodProfileId, direction }` where direction is "RIGHT" or
 * "LEFT" (matching the user_swipes CHECK constraint in the schema).
 */
@Serializable
data class SwipeRequest(
    val recipeId: String,
    val moodProfileId: String,
    val direction: String
)

/** A single tag-point entry returned after a swipe. */
@Serializable
data class TagPointDto(
    val moodProfileKey: String,
    val tagName: String,
    val points: Int
)

/**
 * Response body for POST /functions/v1/swipes:
 * `{ status, swipeId, updatedTagPoints: [] }`.
 */
@Serializable
data class SwipeResponse(
    val status: String,
    val swipeId: String,
    val updatedTagPoints: List<TagPointDto> = emptyList()
)
