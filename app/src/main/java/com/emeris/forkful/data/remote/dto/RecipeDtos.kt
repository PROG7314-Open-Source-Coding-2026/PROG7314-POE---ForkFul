package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.Serializable

//Recipe deck item DTO
@Serializable
data class RecipeDeckItemDto(
    val recipeId: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val prepTimeMinutes: Int = 0,
    val calories: Int = 0,
    val proteinGrams: Int = 0,
    val rating: Double = 0.0,
    val difficulty: String? = null,
    val cuisineType: String? = null,
    val tags: List<String> = emptyList(),
    val topTagPoints: Int = 0,
    val matchPercentage: Int = 0,
    val inPantryCount: Int = 0,
    val totalIngredientsCount: Int = 0,
    val isSaved: Boolean = false,
    val isCooked: Boolean = false
)

//Ingredient DTO
@Serializable
data class IngredientDto(
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val aisleCategory: String? = null,
    val inPantry: Boolean = false
)

//Recipe detail DTO
@Serializable
data class RecipeDetailDto(
    val recipeId: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val calories: Int = 0,
    val proteinGrams: Int = 0,
    val rating: Double = 0.0,
    val difficulty: String? = null,
    val cuisineType: String? = null,
    val dietaryTags: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val ingredients: List<IngredientDto> = emptyList(),
    val isSaved: Boolean = false,
    val isCooked: Boolean = false,
    val matchPercentage: Int = 0,
    val inPantryCount: Int = 0,
    val totalIngredientsCount: Int = 0
)
