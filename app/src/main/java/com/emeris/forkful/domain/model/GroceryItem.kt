package com.emeris.forkful.domain.model

/**
 * Forkful - Grocery Item & Aisle Domain Models
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain representations of shopping list grocery items and category aisle groupings.
 */

data class GroceryItem(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val isChecked: Boolean = false
)

//------------------------------End-Of-File------------------------------//
