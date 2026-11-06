package com.emeris.forkful.domain.model

/**
 * Forkful - Static Choice Catalogs & Option Enumerations
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Provides static option lists for moods, cuisines, prep times, difficulties, languages, and dietary tags.
 */

//Choices catalog
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

    //Explore mood shelves
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

    //Basket aisle order
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

//------------------------------End-Of-File------------------------------//
