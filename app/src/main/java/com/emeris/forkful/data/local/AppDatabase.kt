package com.emeris.forkful.data.local

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
