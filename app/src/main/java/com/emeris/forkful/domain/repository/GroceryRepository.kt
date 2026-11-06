package com.emeris.forkful.domain.repository

/**
 * Forkful - Grocery Basket Domain Repository Contract
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Domain interface declaring grocery basket retrieval, item additions, toggles, and removals.
 */

import com.emeris.forkful.domain.model.GroceryAisle

//Grocery repo contract
interface GroceryRepository {
    suspend fun getBasket(): Result<List<GroceryAisle>>
    suspend fun setChecked(itemId: String, isChecked: Boolean): Result<Boolean>
    suspend fun addItem(name: String, quantity: String?): Result<Boolean>
    suspend fun removeItem(itemId: String): Result<Boolean>
    suspend fun clearItems(checkedOnly: Boolean): Result<Boolean>
}

//------------------------------End-Of-File------------------------------//
