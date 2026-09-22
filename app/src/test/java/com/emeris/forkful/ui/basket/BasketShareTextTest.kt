package com.emeris.forkful.ui.basket

import com.emeris.forkful.domain.model.GroceryAisle
import com.emeris.forkful.domain.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasketShareTextTest {

    @Test
    fun `share text lists aisles and checkboxes per item`() {
        val state = BasketUiState(
            aisles = listOf(
                GroceryAisle(
                    name = "Produce",
                    items = listOf(
                        GroceryItem("g-1", "Tomatoes", "4 large", "Produce", isChecked = false),
                        GroceryItem("g-2", "Basil", "1 bunch", "Produce", isChecked = true)
                    )
                ),
                GroceryAisle(
                    name = "Dairy",
                    items = listOf(GroceryItem("g-3", "Mozzarella", "8 oz", "Dairy", isChecked = false))
                )
            )
        )

        val text = state.asShareText()

        assertTrue(text.startsWith("My Forkful basket"))
        assertTrue(text.contains("PRODUCE"))
        assertTrue(text.contains("- [ ] Tomatoes (4 large)"))
        assertTrue(text.contains("- [x] Basil (1 bunch)"))
        assertTrue(text.contains("DAIRY"))
    }

    @Test
    fun `counts track checked progress`() {
        val state = BasketUiState(
            aisles = listOf(
                GroceryAisle(
                    name = "Pantry",
                    items = listOf(
                        GroceryItem("g-1", "Olive Oil", "500 ml", "Pantry", isChecked = true),
                        GroceryItem("g-2", "Pasta", "500 g", "Pantry", isChecked = false),
                        GroceryItem("g-3", "Flour", "1 kg", "Pantry", isChecked = false)
                    )
                )
            )
        )

        assertEquals(3, state.totalItemCount)
        assertEquals(1, state.checkedItemCount)
    }

    @Test
    fun `empty basket produces a header-only share text`() {
        val text = BasketUiState().asShareText()

        assertEquals("My Forkful basket", text.trim())
    }
}
