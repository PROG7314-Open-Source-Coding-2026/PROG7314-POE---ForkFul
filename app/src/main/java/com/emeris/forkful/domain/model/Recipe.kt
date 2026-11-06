package com.emeris.forkful.domain.model

/**
 * Forkful - Recipe & Ingredient Domain Models
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain representation of recipes, instructional cooking steps, nutrition values, and ingredients.
 */

data class Ingredient(
    val name: String,
    val quantity: String,
    val inPantry: Boolean = true
)

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val prepTimeMinutes: Int,
    val calories: Int,
    val proteinGrams: Int,
    val matchPercentage: Int,
    val inPantryCount: Int,
    val totalIngredientsCount: Int,
    val rating: Double,
    val category: String,
    val isSaved: Boolean = false,
    val isCooked: Boolean = false,
    val dietaryTags: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<String> = emptyList()
)

//------------------------------End-Of-File------------------------------//
