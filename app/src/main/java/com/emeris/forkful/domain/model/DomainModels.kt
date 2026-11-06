package com.emeris.forkful.domain.model

/**
 * Forkful - Domain Model Aggregates & Value Objects
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Defines shared domain value objects including swipe directions, deck queries, and filter modes.
 */

//Swipe direction enum
enum class SwipeDirection(val wireName: String) {
    RIGHT("RIGHT"),
    LEFT("LEFT");

    companion object {
        fun fromWire(name: String): SwipeDirection =
            entries.firstOrNull { it.wireName == name } ?: LEFT
    }
}

//Recipe box filter enum
enum class RecipeBoxFilter(val wireName: String) {
    ALL("ALL"),
    SAVED("SAVED"),
    COOKED("COOKED")
}

//Grocery aisle model
data class GroceryAisle(
    val name: String,
    val items: List<GroceryItem>
)

//Pantry match model
data class PantryMatch(
    val recipe: Recipe,
    val matchPercentage: Int,
    val missingIngredients: List<Ingredient>
)

//Tag point model
data class TagPoint(
    val moodProfileKey: String,
    val tagName: String,
    val points: Int
)

//Swipe outcome model
data class SwipeOutcome(
    val swipeId: String,
    val updatedTagPoints: List<TagPoint>
)

//------------------------------End-Of-File------------------------------//
