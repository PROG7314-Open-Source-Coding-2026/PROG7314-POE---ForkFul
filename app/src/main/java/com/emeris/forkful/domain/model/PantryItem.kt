package com.emeris.forkful.domain.model

/**
 * Forkful - Pantry Item Domain Model
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain representation of an inventory item stored in the user digital pantry.
 */

data class PantryItem(
    val id: String,
    val name: String,
    val category: String,
    val daysUntilExpiry: Int? = null,
    val quantity: String? = null,
    val unit: String? = null,
    val expiryDate: String? = null
) {
    //"500 g" style label for the edit sheet
    val quantityLabel: String get() = listOfNotNull(quantity, unit).joinToString(" ")
}

//------------------------------End-Of-File------------------------------//
