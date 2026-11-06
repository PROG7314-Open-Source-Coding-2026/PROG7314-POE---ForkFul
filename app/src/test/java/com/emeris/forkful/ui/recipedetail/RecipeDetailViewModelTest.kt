package com.emeris.forkful.ui.recipedetail

import com.emeris.forkful.domain.model.Ingredient
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.ui.swipestack.FakeRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailViewModelTest {

    private lateinit var repository: FakeRecipeRepository
    private lateinit var viewModel: RecipeDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeRecipeRepository()
        viewModel = RecipeDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `heart saves an unsaved recipe`() = runTest {
        repository.deck = listOf(recipe(isSaved = false))
        viewModel.load("r-1")

        viewModel.toggleSave()

        assertTrue(viewModel.state.value.recipe!!.isSaved)
        assertEquals(SwipeDirection.RIGHT, repository.lastSwipeDirection)
    }

    @Test
    fun `heart unsaves a saved recipe`() = runTest {
        repository.deck = listOf(recipe(isSaved = true))
        viewModel.load("r-1")

        viewModel.toggleSave()

        assertFalse(viewModel.state.value.recipe!!.isSaved)
        assertEquals(listOf("r-1"), repository.unsavedIds)
        assertEquals("Removed from your Recipe Box", viewModel.state.value.actionMessage)
    }

    @Test
    fun `failed unsave restores the heart and keeps the recipe on screen`() = runTest {
        repository.deck = listOf(recipe(isSaved = true))
        repository.failUnsave = true
        viewModel.load("r-1")

        viewModel.toggleSave()

        assertTrue(viewModel.state.value.recipe!!.isSaved)
        assertNull(viewModel.state.value.errorMessage)
        assertEquals("Network down", viewModel.state.value.actionMessage)
    }

    private fun recipe(isSaved: Boolean) = Recipe(
        id = "r-1",
        title = "Tomato bredie",
        description = "Test dish",
        imageUrl = "https://example.com/r-1.jpg",
        prepTimeMinutes = 20,
        calories = 400,
        proteinGrams = 20,
        matchPercentage = 90,
        inPantryCount = 3,
        totalIngredientsCount = 5,
        rating = 4.5,
        category = "Comfort",
        isSaved = isSaved,
        ingredients = listOf(Ingredient("Tomatoes", "4", inPantry = true))
    )
}

//------------------------------End-Of-File------------------------------//
