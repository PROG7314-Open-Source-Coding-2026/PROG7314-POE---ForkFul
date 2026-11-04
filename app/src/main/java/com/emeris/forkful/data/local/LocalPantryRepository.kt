package com.emeris.forkful.data.local

import android.content.Context
import com.emeris.forkful.domain.model.PantryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class LocalPantryRepository(context: Context) {

    private val dao = DatabaseProvider.getDatabase(context).pantryDao()

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

    suspend fun add(name: String, category: String = "Other") {
        val entity = PantryItemEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            category = category,
            isPendingSync = true
        )
        dao.insert(entity)
    }

    suspend fun remove(id: String) {
        dao.deleteById(id)
    }
}