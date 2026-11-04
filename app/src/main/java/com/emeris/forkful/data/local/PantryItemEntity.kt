package com.emeris.forkful.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val quantity: String? = null,
    val unit: String? = null,
    val daysUntilExpiry: Int? = null,
    val isPendingSync: Boolean = false
)