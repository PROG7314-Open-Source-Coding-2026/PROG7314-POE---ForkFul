package com.emeris.forkful.data.repository

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.data.remote.dto.ToggleGroceryItemRequest
import com.emeris.forkful.domain.model.GroceryAisle
import com.emeris.forkful.domain.repository.GroceryRepository

//Grocery repo impl
class GroceryRepositoryImpl(
    private val api: ForkfulApi
) : GroceryRepository {

    override suspend fun getBasket(): Result<List<GroceryAisle>> = runCatching {
        ForkfulLogger.logNetwork("groceries", "GET")
        api.groceries().map { it.toDomain() }
    }

    override suspend fun setChecked(itemId: String, isChecked: Boolean): Result<Boolean> = runCatching {
        ForkfulLogger.logAction("BASKET", "toggle $itemId -> $isChecked")
        api.toggleGroceryItem(itemId, ToggleGroceryItemRequest(isChecked))
        true
    }
}
