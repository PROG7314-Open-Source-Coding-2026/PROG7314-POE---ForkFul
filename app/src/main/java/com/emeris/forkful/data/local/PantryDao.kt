package com.emeris.forkful.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {

    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    fun getAll(): Flow<List<PantryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PantryItemEntity)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM pantry_items WHERE isPendingSync = 1")
    suspend fun getPendingSync(): List<PantryItemEntity>

    @Query("UPDATE pantry_items SET isPendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)
}