package com.emeris.forkful.data.repository

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.core.util.Validators
import com.emeris.forkful.data.remote.dto.AddGroceryItemRequest
import com.emeris.forkful.data.remote.dto.ToggleGroceryItemRequest
import com.emeris.forkful.domain.model.GroceryAisle
import com.emeris.forkful.domain.repository.GroceryRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

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

    override suspend fun addItem(name: String, quantity: String?): Result<Boolean> = runCatching {
        require(Validators.isNotBlank(name)) { "Item name cannot be empty." }
        val trimmed = quantity?.trim().orEmpty()
        val parts = trimmed.split(Regex("\\s+"), limit = 2).filter { it.isNotBlank() }
        //"2 tins" -> quantity 2, unit tins; "a bunch" -> unit only
        val (qty, unit) = when {
            parts.isEmpty() -> null to null
            parts[0].replace(",", ".").toDoubleOrNull() != null -> parts[0] to parts.getOrNull(1)
            else -> null to trimmed
        }
        ForkfulLogger.logAction("BASKET", "add $name ($trimmed)")
        try {
            api.addGroceryItem(AddGroceryItemRequest(name.trim(), qty, unit))
        } catch (e: HttpException) {
            throw Exception(e.serverMessage(), e)
        }
        true
    }

    override suspend fun removeItem(itemId: String): Result<Boolean> = runCatching {
        ForkfulLogger.logAction("BASKET", "remove $itemId")
        api.deleteGroceryItem(itemId)
        true
    }

    override suspend fun clearItems(checkedOnly: Boolean): Result<Boolean> = runCatching {
        ForkfulLogger.logAction("BASKET", "clear ${if (checkedOnly) "checked" else "all"}")
        api.clearGroceries(if (checkedOnly) "checked" else "all")
        true
    }

    //Server sends { "error": "..." } for 4xx responses
    private fun HttpException.serverMessage(): String {
        val body = response()?.errorBody()?.string().orEmpty()
        return runCatching {
            Json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
        }.getOrNull() ?: "Could not add the item (${code()})"
    }
}

//------------------------------End-Of-File------------------------------//
