package com.emeris.forkful.data.mapper

import com.emeris.forkful.core.util.PantryUtils
import com.emeris.forkful.data.remote.dto.GroceryAisleDto
import com.emeris.forkful.data.remote.dto.IngredientDto
import com.emeris.forkful.data.remote.dto.PantryItemDto
import com.emeris.forkful.data.remote.dto.RecipeDeckItemDto
import com.emeris.forkful.data.remote.dto.RecipeDetailDto
import com.emeris.forkful.data.remote.dto.UserPreferencesDto
import com.emeris.forkful.domain.model.GroceryAisle
import com.emeris.forkful.domain.model.GroceryItem
import com.emeris.forkful.domain.model.Ingredient
import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch
import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.TagPoint
import com.emeris.forkful.domain.model.UserPreferences
import com.emeris.forkful.data.remote.dto.PantryMatchDto
import com.emeris.forkful.data.remote.dto.SwipeResponse
import com.emeris.forkful.data.remote.dto.TagPointDto
import com.emeris.forkful.data.remote.dto.UpdatePreferencesRequest
import com.emeris.forkful.domain.model.SwipeOutcome

/** DTO -> domain mappers keeping the UI layer free of wire formats. */

fun RecipeDeckItemDto.toDomain(): Recipe = Recipe(
    id = recipeId,
    title = title,
    description = description.orEmpty(),
    imageUrl = imageUrl.orEmpty(),
    prepTimeMinutes = prepTimeMinutes,
    calories = calories,
    proteinGrams = proteinGrams,
    matchPercentage = matchPercentage,
    inPantryCount = inPantryCount,
    totalIngredientsCount = totalIngredientsCount,
    rating = rating,
    category = cuisineType ?: "Other",
    isSaved = isSaved,
    isCooked = isCooked,
    tags = tags,
    ingredients = emptyList()
)

fun RecipeDetailDto.toDomain(): Recipe = Recipe(
    id = recipeId,
    title = title,
    description = description.orEmpty(),
    imageUrl = imageUrl.orEmpty(),
    prepTimeMinutes = prepTimeMinutes,
    calories = calories,
    proteinGrams = proteinGrams,
    matchPercentage = matchPercentage,
    inPantryCount = inPantryCount,
    totalIngredientsCount = totalIngredientsCount,
    rating = rating,
    category = cuisineType ?: "Other",
    isSaved = isSaved,
    isCooked = isCooked,
    tags = tags,
    ingredients = ingredients.map { it.toDomain() },
    instructions = instructions
)

fun IngredientDto.toDomain(): Ingredient = Ingredient(
    name = name,
    quantity = buildString {
        quantity?.let { append(it) }
        unit?.takeIf { it.isNotBlank() }?.let { if (isNotEmpty()) append(' ') ; append(it) }
    }.ifBlank { "1" },
    inPantry = inPantry
)

fun PantryItemDto.toDomain(): PantryItem = PantryItem(
    id = pantryItemId,
    name = name,
    category = category ?: "Other",
    daysUntilExpiry = PantryUtils.daysUntilExpiry(expiryDate)
)

fun PantryMatchDto.toDomain(): PantryMatch = PantryMatch(
    recipe = recipe.toDomain(),
    matchPercentage = matchPercentage,
    missingIngredients = missingIngredients.map { it.toDomain() }
)

fun GroceryAisleDto.toDomain(): GroceryAisle = GroceryAisle(
    name = aisle,
    items = items.map { dto ->
        GroceryItem(
            id = dto.itemId,
            name = dto.name,
            quantity = listOfNotNull(dto.quantity, dto.unit)
                .joinToString(" ")
                .ifBlank { "1" },
            category = aisle,
            isChecked = dto.isChecked
        )
    }
)

fun UserPreferencesDto.toDomain(): UserPreferences = UserPreferences(
    language = language,
    dietaryTags = dietaryTags,
    notificationsEnabled = notificationsEnabled,
    theme = theme,
    onboarded = onboarded,
    biometricLockEnabled = biometricLockEnabled,
    email = email,
    displayName = displayName
)

fun PreferencesUpdate.toRequest(): UpdatePreferencesRequest = UpdatePreferencesRequest(
    language = language,
    dietaryTags = dietaryTags,
    notificationsEnabled = notificationsEnabled,
    theme = theme,
    onboarded = onboarded,
    biometricLockEnabled = biometricLockEnabled,
    seedCuisines = seedCuisines,
    resetTasteProfiles = resetTasteProfiles
)

fun SwipeResponse.toOutcome(): SwipeOutcome = SwipeOutcome(
    swipeId = swipeId,
    updatedTagPoints = updatedTagPoints.map { it.toDomain() }
)

fun TagPointDto.toDomain(): TagPoint = TagPoint(
    moodProfileKey = moodProfileKey,
    tagName = tagName,
    points = points
)
