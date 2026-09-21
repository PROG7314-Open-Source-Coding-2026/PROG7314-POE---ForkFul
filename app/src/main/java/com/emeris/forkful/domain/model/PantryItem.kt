package com.emeris.forkful.domain.model

data class PantryItem(
    val id: String,
    val name: String,
    val category: String,
    val daysUntilExpiry: Int? = null
)