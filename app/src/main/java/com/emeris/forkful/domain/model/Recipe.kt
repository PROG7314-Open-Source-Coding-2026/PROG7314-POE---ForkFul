package com.emeris.forkful.domain.model

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
    val tags: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList()
)