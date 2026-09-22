package com.emeris.forkful.domain.model

/**
 * Central catalogue of selectable cuisines (mood profiles), dietary filters
 * and languages used across Onboarding, Explore and Settings.
 *
 * Mood profile keys are lowercase single words and map 1:1 to the
 * `mood_profile_key` column used by the tag-point engine.
 */
object ChoiceCatalog {

    data class Choice(val key: String, val label: String, val emoji: String)

    val cuisines = listOf(
        Choice("italian", "Italian", "🍝"),
        Choice("asian", "Asian", "🥡"),
        Choice("mexican", "Mexican", "🌮"),
        Choice("braai", "Braai", "🔥"),
        Choice("comfort", "Comfort", "🥘"),
        Choice("healthy", "Healthy", "🥗"),
        Choice("sweets", "Sweets", "🍰"),
        Choice("vegan", "Vegan", "🌱")
    )

    val dietaryOptions = listOf(
        "Vegetarian",
        "Vegan",
        "Halaal",
        "No Shellfish",
        "Gluten-Free"
    )

    /** Curated mood shelf shown on the Explore home hub (FR-11). */
    val moodShelves = listOf(
        Choice("italian", "Italian night", "🍝"),
        Choice("comfort", "Comfort food", "🥘"),
        Choice("healthy", "Something healthy", "🥗"),
        Choice("pantry", "Use up your pantry", "🧺"),
        Choice("sweets", "Sweet tooth", "🍰")
    )

    val prepTimeOptions = listOf(15, 30, 45, 60)

    val difficultyOptions = listOf("Easy", "Medium", "Hard")

    val languageOptions = listOf(
        Choice("en", "English", "🇬🇧"),
        Choice("zu", "isiZulu", "🇿🇦"),
        Choice("af", "Afrikaans", "🇿🇦")
    )

    /** Aisle order used when rendering the grouped basket (FR-23). */
    val aisleOrder = listOf(
        "Produce",
        "Bakery",
        "Butchery",
        "Dairy",
        "Deli",
        "Seafood",
        "Frozen",
        "Pantry",
        "Dry Goods",
        "Condiments",
        "Spices",
        "Baking",
        "Beverages",
        "Other"
    )
}
