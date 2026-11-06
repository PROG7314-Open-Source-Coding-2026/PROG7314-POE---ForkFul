package com.emeris.forkful.domain.model

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