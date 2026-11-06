package com.emeris.forkful.data.repository

/**
 * Forkful - Pantry Repository Implementation
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Implements pantry inventory management, recipe match calculations, and remote synchronization.
 */

import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.core.util.PantryUtils
import com.emeris.forkful.data.mapper.toDomain
import com.emeris.forkful.data.remote.dto.AddPantryItemRequest
import com.emeris.forkful.data.remote.dto.PantryMatchRequest
import com.emeris.forkful.data.remote.dto.UpdatePantryItemRequest
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch
import com.emeris.forkful.domain.repository.PantryRepository
import com.emeris.forkful.core.util.Validators

//Pantry repo impl
class PantryRepositoryImpl(
    private val api: ForkfulApi
) : PantryRepository {

    override suspend fun getPantry(): Result<List<PantryItem>> = runCatching {
        ForkfulLogger.logNetwork("pantry", "GET")
        api.pantry().map { it.toDomain() }
    }

    override suspend fun addItem(
        name: String,
        quantity: String?,
        unit: String?,
        expiryDate: String?
    ): Result<PantryItem> = runCatching {
        require(Validators.isNotBlank(name)) { "Ingredient name cannot be empty." }
        ForkfulLogger.logNetwork("pantry", "POST item=$name")
        val response = api.addPantryItem(
            AddPantryItemRequest(
                name = name.trim(),
                quantity = quantity?.trim()?.takeIf { it.isNotEmpty() },
                unit = unit?.trim()?.takeIf { it.isNotEmpty() },
                expiryDate = expiryDate?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        PantryItem(
            id = response.pantryItemId,
            name = name.trim(),
            category = "Other",
            daysUntilExpiry = PantryUtils.daysUntilExpiry(expiryDate)
        )
    }

    override suspend fun updateItem(
        pantryItemId: String,
        name: String,
        quantity: String?,
        unit: String?,
        expiryDate: String?
    ): Result<Boolean> = runCatching {
        require(Validators.isNotBlank(name)) { "Ingredient name cannot be empty." }
        ForkfulLogger.logNetwork("pantry", "PUT id=$pantryItemId name=$name")
        api.updatePantryItem(
            pantryItemId,
            UpdatePantryItemRequest(
                name = name.trim(),
                quantity = quantity?.trim()?.takeIf { it.isNotEmpty() },
                unit = unit?.trim()?.takeIf { it.isNotEmpty() },
                expiryDate = expiryDate?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        true
    }

    override suspend fun removeItem(pantryItemId: String): Result<Boolean> = runCatching {
        ForkfulLogger.logNetwork("pantry", "DELETE id=$pantryItemId")
        api.deletePantryItem(pantryItemId)
        true
    }

    override suspend fun getMatches(missingThreshold: Int): Result<List<PantryMatch>> = runCatching {
        ForkfulLogger.logNetwork("pantry-match", "missingThreshold=$missingThreshold")
        api.pantryMatch(PantryMatchRequest(missingThreshold)).map { it.toDomain() }
    }
}

//------------------------------End-Of-File------------------------------//
