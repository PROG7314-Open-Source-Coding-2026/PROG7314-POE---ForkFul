package com.emeris.forkful.domain.model

/** Swipe directions understood by the taste-profile engine (FR-07/FR-08). */
enum class SwipeDirection(val wireName: String) {
    RIGHT("RIGHT"),
    LEFT("LEFT");

    companion object {
        fun fromWire(name: String): SwipeDirection =
            entries.firstOrNull { it.wireName == name } ?: LEFT
    }
}

/** Filter tabs on the Recipe Box screen (FR-16). */
enum class RecipeBoxFilter(val wireName: String) {
    ALL("ALL"),
    SAVED("SAVED"),
    COOKED("COOKED")
}

/** A grocery list grouped by retail aisle (FR-23). */
data class GroceryAisle(
    val name: String,
    val items: List<GroceryItem>
)

/** One pantry-match result (FR-20). */
data class PantryMatch(
    val recipe: Recipe,
    val matchPercentage: Int,
    val missingIngredients: List<Ingredient>
)

/** A single taste-profile tag point updated after a swipe. */
data class TagPoint(
    val moodProfileKey: String,
    val tagName: String,
    val points: Int
)

/** Result of recording a swipe against the REST API. */
data class SwipeOutcome(
    val swipeId: String,
    val updatedTagPoints: List<TagPoint>
)
