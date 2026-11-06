package com.emeris.forkful.ui.basket

import com.emeris.forkful.domain.model.GroceryAisle
import com.emeris.forkful.domain.model.GroceryItem
import com.emeris.forkful.domain.repository.GroceryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeGroceryRepository : GroceryRepository {
    var aisles = listOf(
        GroceryAisle(
            "Produce",
            listOf(
                GroceryItem("g-1", "Onions", "2", "Produce"),
                GroceryItem("g-2", "Garlic", "1", "Produce", isChecked = true)
            )
        ),
        GroceryAisle("Dairy", listOf(GroceryItem("g-3", "Milk", "1 l", "Dairy")))
    )
    val removed = mutableListOf<String>()
    var cleared: Boolean? = null
    var added: Pair<String, String?>? = null
    var fail = false

    override suspend fun getBasket(): Result<List<GroceryAisle>> = Result.success(aisles)
    override suspend fun setChecked(itemId: String, isChecked: Boolean) = Result.success(true)

    override suspend fun addItem(name: String, quantity: String?): Result<Boolean> {
        if (fail) return Result.failure(Exception("$name is already in your basket"))
        added = name to quantity
        return Result.success(true)
    }

    override suspend fun removeItem(itemId: String): Result<Boolean> {
        if (fail) return Result.failure(Exception("Offline"))
        removed += itemId
        return Result.success(true)
    }

    override suspend fun clearItems(checkedOnly: Boolean): Result<Boolean> {
        cleared = checkedOnly
        return Result.success(true)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BasketViewModelTest {

    private lateinit var repository: FakeGroceryRepository
    private lateinit var viewModel: BasketViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeGroceryRepository()
        viewModel = BasketViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removing an item takes it off the list`() = runTest {
        viewModel.removeItem("g-3")

        assertEquals(listOf("g-3"), repository.removed)
        assertEquals(2, viewModel.state.value.totalItemCount)
        assertTrue(viewModel.state.value.aisles.none { it.name == "Dairy" })
        assertEquals("Milk removed", viewModel.state.value.snackbarMessage)
    }

    @Test
    fun `failed remove puts the item back`() = runTest {
        repository.fail = true

        viewModel.removeItem("g-1")

        assertEquals(3, viewModel.state.value.totalItemCount)
        assertEquals("Offline", viewModel.state.value.snackbarMessage)
    }

    @Test
    fun `items can be added by hand`() = runTest {
        var closed = false

        viewModel.addItem(" Dish soap ", "2 bottles") { closed = true }

        assertEquals("Dish soap" to "2 bottles", repository.added)
        assertTrue(closed)
        assertEquals("Dish soap added to your basket", viewModel.state.value.snackbarMessage)
    }

    @Test
    fun `duplicate manual add shows the server message`() = runTest {
        repository.fail = true

        viewModel.addItem("Onions", "")

        assertEquals("Onions is already in your basket", viewModel.state.value.snackbarMessage)
    }

    @Test
    fun `remove collected clears only ticked items`() = runTest {
        viewModel.clearCollected()

        assertEquals(true, repository.cleared)
        assertEquals(2, viewModel.state.value.totalItemCount)
        assertEquals(0, viewModel.state.value.checkedItemCount)
    }

    @Test
    fun `clear basket empties the list`() = runTest {
        viewModel.clearAll()

        assertEquals(false, repository.cleared)
        assertEquals(0, viewModel.state.value.totalItemCount)
    }
}

//------------------------------End-Of-File------------------------------//
