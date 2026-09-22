package com.emeris.forkful.data.mapper

import com.emeris.forkful.data.remote.dto.GroceryAisleDto
import com.emeris.forkful.data.remote.dto.GroceryItemDto
import com.emeris.forkful.data.remote.dto.IngredientDto
import com.emeris.forkful.data.remote.dto.PantryItemDto
import com.emeris.forkful.data.remote.dto.RecipeDeckItemDto
import com.emeris.forkful.data.remote.dto.RecipeDetailDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MappersTest {

    @Test
    fun `deck dto maps onto the recipe domain model`() {
        val dto = RecipeDeckItemDto(
            recipeId = "r-1",
            title = "Peri-peri chicken bowls",
            description = "Spicy flame roasted chicken",
            imageUrl = "https://example.com/img.jpg",
            prepTimeMinutes = 25,
            calories = 510,
            proteinGrams = 42,
            rating = 4.8,
            difficulty = "Easy",
            cuisineType = "Braai",
            tags = listOf("Chicken +22", "Spicy +14"),
            topTagPoints = 22,
            matchPercentage = 98,
            inPantryCount = 4,
            totalIngredientsCount = 5,
            isSaved = true,
            isCooked = false
        )

        val recipe = dto.toDomain()

        assertEquals("r-1", recipe.id)
        assertEquals("Peri-peri chicken bowls", recipe.title)
        assertEquals("Braai", recipe.category)
        assertEquals(4.8, recipe.rating, 0.001)
        assertTrue(recipe.isSaved)
        assertEquals(listOf("Chicken +22", "Spicy +14"), recipe.tags)
    }

    @Test
    fun `ingredient quantity and unit are joined into one display string`() {
        assertEquals("500 g", IngredientDto("Flour", "500", "g").toDomain().quantity)
        assertEquals("500", IngredientDto("Flour", "500", null).toDomain().quantity)
        assertEquals("g", IngredientDto("Flour", null, "g").toDomain().quantity)
        assertEquals("1", IngredientDto("Flour", null, null).toDomain().quantity)
    }

    @Test
    fun `pantry dto computes days until expiry from iso date`() {
        val inThreeDays = LocalDate.now().plusDays(3).toString()
        val item = PantryItemDto("p-1", "Spinach", "200", "g", inThreeDays, "Produce").toDomain()

        assertEquals("p-1", item.id)
        assertEquals(3, item.daysUntilExpiry)
        assertEquals("Produce", item.category)
    }

    @Test
    fun `grocery aisle dto maps to grouped domain items`() {
        val dto = GroceryAisleDto(
            aisle = "Dairy",
            items = listOf(
                GroceryItemDto("g-1", "Milk", "1", "litre", false),
                GroceryItemDto("g-2", "Butter", "250", "g", true)
            )
        )

        val aisle = dto.toDomain()

        assertEquals("Dairy", aisle.name)
        assertEquals(2, aisle.items.size)
        assertEquals("1 litre", aisle.items[0].quantity)
        assertTrue(aisle.items[1].isChecked)
    }

    @Test
    fun `detail dto carries instructions through to the domain`() {
        val dto = RecipeDetailDto(
            recipeId = "r-9",
            title = "Tomato bredie",
            instructions = listOf("Brown the mutton", "Simmer for an hour"),
            ingredients = listOf(IngredientDto("Mutton", "500", "g", "Butchery", true))
        )

        val recipe = dto.toDomain()

        assertEquals(2, recipe.instructions.size)
        assertTrue(recipe.ingredients.first().inPantry)
    }
}
