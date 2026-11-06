package com.emeris.forkful.domain.repository

/**
 * Forkful - Pantry Domain Repository Contract
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain interface declaring pantry queries, item additions, edits, deletions, and recipe matches.
 */

import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch

//Pantry repo contract
interface PantryRepository {
    suspend fun getPantry(): Result<List<PantryItem>>
    suspend fun addItem(name: String, quantity: String?, unit: String?, expiryDate: String?): Result<PantryItem>
    suspend fun updateItem(pantryItemId: String, name: String, quantity: String?, unit: String?, expiryDate: String?): Result<Boolean>
    suspend fun removeItem(pantryItemId: String): Result<Boolean>
    suspend fun getMatches(missingThreshold: Int = 2): Result<List<PantryMatch>>
}

//------------------------------End-Of-File------------------------------//
