package com.emeris.forkful.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

object StringOrNumericSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrNumeric", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
        val element = jsonDecoder.decodeJsonElement()
        return (element as? JsonPrimitive)?.content ?: element.toString()
    }
}

// Ingredient DTO
@Serializable
data class IngredientDto(
    val name: String,
    @Serializable(with = StringOrNumericSerializer::class)
    val quantity: String? = null,
    val unit: String? = null,
    val aisleCategory: String? = null,
    val inPantry: Boolean = false
)

// Recipe deck item DTO
@Serializable
data class RecipeDeckItemDto(
    val recipeId: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val prepTimeMinutes: Int = 0,
    val calories: Int = 0,
    val proteinGrams: Int = 0,
    val rating: Double = 0.0,
    val difficulty: String? = null,
    val cuisineType: String? = null,
    val dietaryTags: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val topTagPoints: Int = 0,
    val matchPercentage: Int = 0,
    val inPantryCount: Int = 0,
    val totalIngredientsCount: Int = 0,
    val isSaved: Boolean = false,
    val isCooked: Boolean = false,
    val ingredients: List<IngredientDto> = emptyList()
)

// Recipe detail DTO
@Serializable
data class RecipeDetailDto(
    val recipeId: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val calories: Int = 0,
    val proteinGrams: Int = 0,
    val rating: Double = 0.0,
    val difficulty: String? = null,
    val cuisineType: String? = null,
    val dietaryTags: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val ingredients: List<IngredientDto> = emptyList(),
    val isSaved: Boolean = false,
    val isCooked: Boolean = false,
    val matchPercentage: Int = 0,
    val inPantryCount: Int = 0,
    val totalIngredientsCount: Int = 0
)