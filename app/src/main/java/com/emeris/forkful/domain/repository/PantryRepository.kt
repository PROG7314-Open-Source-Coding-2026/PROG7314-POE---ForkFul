package com.emeris.forkful.domain.repository

import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch

//Pantry repo contract
interface PantryRepository {
    suspend fun getPantry(): Result<List<PantryItem>>
    suspend fun addItem(name: String, quantity: String?, unit: String?, expiryDate: String?): Result<PantryItem>
    suspend fun removeItem(pantryItemId: String): Result<Boolean>
    suspend fun getMatches(missingThreshold: Int = 2): Result<List<PantryMatch>>
}
