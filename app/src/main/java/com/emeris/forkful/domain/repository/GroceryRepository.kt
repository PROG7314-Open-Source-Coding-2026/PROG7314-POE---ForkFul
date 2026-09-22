package com.emeris.forkful.domain.repository

import com.emeris.forkful.domain.model.GroceryAisle

/** Smart Grocery Aggregator contract (FR-21 .. FR-24). */
interface GroceryRepository {
    suspend fun getBasket(): Result<List<GroceryAisle>>
    suspend fun setChecked(itemId: String, isChecked: Boolean): Result<Boolean>
}
