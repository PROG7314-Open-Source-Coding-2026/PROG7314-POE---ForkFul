package com.emeris.forkful.data.local

import android.content.Context
import com.emeris.forkful.domain.model.PantryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Local Room cache repository for kitchen pantry stock.
 * Provides offline caching, pending sync tracking, and emits reactive flows
 * to support seamless offline operation.
 *
 * @param context Application context used to initialise [AppDatabase]
 */
class LocalPantryRepository(context: Context) {

    private val dao = DatabaseProvider.getDatabase(context).pantryDao()

    /**
     * Emits a reactive stream of all locally cached pantry items mapped to domain models.
     */
    fun getAll(): Flow<List<PantryItem>> = dao.getAll().map { entities ->
        entities.map { entity ->
            PantryItem(
                id = entity.id,
                name = entity.name,
                category = entity.category,
                daysUntilExpiry = entity.daysUntilExpiry
            )
        }
    }

    /**
     * Inserts a new pantry item into the local Room database, marked as pending sync.
     *
     * @param name Ingredient title
     * @param category Aisle category name
     */
    suspend fun add(name: String, category: String = "Other") {
        val entity = PantryItemEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            category = category,
            isPendingSync = true
        )
        dao.insert(entity)
    }

    /**
     * Deletes a local pantry item by its unique identifier.
     *
     * @param id The entity UUID
     */
    suspend fun remove(id: String) {
        dao.deleteById(id)
    }

    /**
     * Retrieves all locally added records awaiting remote synchronisation with Supabase.
     */
    suspend fun getPendingSync(): List<PantryItemEntity> = dao.getPendingSync()

    /**
     * Marks a local record as synchronised after successful remote post.
     *
     * @param id The entity UUID
     */
    suspend fun markSynced(id: String) = dao.markSynced(id)
}

//------------------------------End-Of-File------------------------------//
