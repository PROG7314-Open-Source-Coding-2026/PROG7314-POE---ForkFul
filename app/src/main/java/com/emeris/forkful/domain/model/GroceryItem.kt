package com.emeris.forkful.domain.model

data class GroceryItem(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val isChecked: Boolean = false
)