package com.emeris.forkful.data.local

/**
 * Forkful - Pantry Room Entity Schema
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Room table definition storing pantry item metadata, quantities, expiry dates, and categories.
 */

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

//------------------------------End-Of-File------------------------------//
