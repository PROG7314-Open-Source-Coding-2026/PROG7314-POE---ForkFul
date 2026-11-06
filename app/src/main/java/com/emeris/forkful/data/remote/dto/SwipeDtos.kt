package com.emeris.forkful.data.remote.dto

/**
 * Forkful - Swipe Interaction Data Transfer Objects (DTOs)
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Data transfer schemas for user swipe gestures, preference tag point updates, and outcome scores.
 */

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

//------------------------------End-Of-File------------------------------//
