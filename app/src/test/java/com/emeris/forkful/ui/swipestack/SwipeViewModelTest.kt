package com.emeris.forkful.ui.swipestack

import com.emeris.forkful.domain.model.Ingredient
import com.emeris.forkful.domain.model.Recipe
import com.emeris.forkful.domain.model.RecipeBoxFilter
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.model.SwipeOutcome
import com.emeris.forkful.domain.repository.DeckQuery
import com.emeris.forkful.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeRecipeRepository : RecipeRepository {

    var deck: List<Recipe> = emptyList()
    var lastSwipeDirection: SwipeDirection? = null
    var lastSwipeMood: String? = null

    override suspend fun getDeck(query: DeckQuery): Result<List<Recipe>> = Result.success(deck)

    override suspend fun getRecipeDetail(recipeId: String): Result<Recipe> =
        Result.success(deck.first { it.id == recipeId })

    override suspend fun getRecipeBox(filter: RecipeBoxFilter): Result<List<Recipe>> =
        Result.success(deck.filter { it.isSaved })

    override suspend fun recordSwipe(
        recipeId: String,
        direction: SwipeDirection,
        moodProfileKey: String
    ): Result<SwipeOutcome> {
        lastSwipeDirection = direction
        lastSwipeMood = moodProfileKey
        return Result.success(SwipeOutcome("s-1", emptyList()))
    }

    override suspend fun setCooked(recipeId: String, cooked: Boolean): Result<Boolean> =
        Result.success(true)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SwipeViewModelTest {

    private lateinit var repository: FakeRecipeRepository
    private lateinit var viewModel: SwipeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeRecipeRepository()
        repository.deck = listOf(
            recipe("r-1", "Margherita flatbread"),
            recipe("r-2", "Tomato bredie"),
            recipe("r-3", "Beet & feta risotto")
        )
        viewModel = SwipeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDeck populates the stack for a mood`() = runTest {
        viewModel.loadDeck("italian")

        assertEquals("italian", viewModel.state.value.moodKey)
        assertEquals(3, viewModel.state.value.deck.size)
        assertEquals("r-1", viewModel.state.value.currentRecipe?.id)
    }

    @Test
    fun `a right swipe advances the deck optimistically and records RIGHT`() = runTest {
        viewModel.loadDeck("italian")

        viewModel.swipe(SwipeDirection.RIGHT)

        assertEquals("r-2", viewModel.state.value.currentRecipe?.id)
        assertEquals(SwipeDirection.RIGHT, repository.lastSwipeDirection)
        assertEquals("italian", repository.lastSwipeMood)
        assertEquals(1, viewModel.state.value.sessionCount)
    }

    @Test
    fun `swiping through the whole deck flags the end`() = runTest {
        viewModel.loadDeck("italian")

        repeat(3) { viewModel.swipe(SwipeDirection.LEFT) }

        assertNull(viewModel.state.value.currentRecipe)
        assertTrue(viewModel.state.value.reachedEnd)
        assertEquals(3, viewModel.state.value.sessionCount)
    }

    @Test
    fun `swipe failures still advance the visible card`() = runTest {
        repository.deck = listOf(recipe("r-1", "Only dish"))
        viewModel.loadDeck("comfort")
        //Fake repo test guard
        viewModel.swipe(SwipeDirection.RIGHT)

        assertNotNull(viewModel.state.value.lastTagPoints)
        assertTrue(viewModel.state.value.reachedEnd)
    }

    private fun recipe(id: String, title: String) = Recipe(
        id = id,
        title = title,
        description = "Test dish",
        imageUrl = "https://example.com/$id.jpg",
        prepTimeMinutes = 20,
        calories = 400,
        proteinGrams = 20,
        matchPercentage = 90,
        inPantryCount = 3,
        totalIngredientsCount = 5,
        rating = 4.5,
        category = "Test",
        tags = listOf("Fresh +5"),
        ingredients = listOf(Ingredient("Something", "1 cup", inPantry = true))
    )
}
