package com.emeris.forkful.data.local

/**
 * Forkful - Room Database Instance & Schema Configuration
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Defines Room database tables, version migrations, and DAO accessors for offline caching.
 */

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PantryItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
}

//------------------------------End-Of-File------------------------------//
