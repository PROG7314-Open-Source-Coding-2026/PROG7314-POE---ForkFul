package com.emeris.forkful.domain.repository

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
