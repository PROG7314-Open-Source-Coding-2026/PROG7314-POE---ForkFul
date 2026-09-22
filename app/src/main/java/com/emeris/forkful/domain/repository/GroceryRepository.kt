package com.emeris.forkful.domain.repository

import com.emeris.forkful.domain.model.GroceryAisle

//Grocery repo contract
interface GroceryRepository {
    suspend fun getBasket(): Result<List<GroceryAisle>>
    suspend fun setChecked(itemId: String, isChecked: Boolean): Result<Boolean>
}
