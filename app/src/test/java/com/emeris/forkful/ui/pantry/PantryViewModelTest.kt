package com.emeris.forkful.ui.pantry

import com.emeris.forkful.domain.model.PantryItem
import com.emeris.forkful.domain.model.PantryMatch
import com.emeris.forkful.domain.repository.PantryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

private data class UpdateCall(
    val id: String,
    val name: String,
    val quantity: String?,
    val unit: String?,
    val expiryDate: String?
)

private class FakePantryRepository : PantryRepository {
    var items = listOf(
        PantryItem("p-1", "Salt", "Spices", quantity = "1", unit = "kg", expiryDate = "2027-01-01")
    )
    var lastUpdate: UpdateCall? = null
    var failUpdate = false

    override suspend fun getPantry(): Result<List<PantryItem>> = Result.success(items)
    override suspend fun addItem(name: String, quantity: String?, unit: String?, expiryDate: String?) =
        Result.success(PantryItem("p-new", name, "Other"))

    override suspend fun updateItem(
        pantryItemId: String,
        name: String,
        quantity: String?,
        unit: String?,
        expiryDate: String?
    ): Result<Boolean> {
        if (failUpdate) return Result.failure(Exception("Server unavailable"))
        lastUpdate = UpdateCall(pantryItemId, name, quantity, unit, expiryDate)
        return Result.success(true)
    }

    override suspend fun removeItem(pantryItemId: String) = Result.success(true)
    override suspend fun getMatches(missingThreshold: Int): Result<List<PantryMatch>> = Result.success(emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class PantryViewModelTest {

    private lateinit var repository: FakePantryRepository
    private lateinit var viewModel: PantryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakePantryRepository()
        viewModel = PantryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `renaming an item keeps its expiry when none is picked`() = runTest {
        val item = viewModel.state.value.items.single()

        viewModel.updatePantryItem(item, "Sea salt", "500 g", daysUntilExpiry = null)

        assertEquals(UpdateCall("p-1", "Sea salt", "500", "g", "2027-01-01"), repository.lastUpdate)
        val updated = viewModel.state.value.items.single()
        assertEquals("Sea salt", updated.name)
        assertEquals("500 g", updated.quantityLabel)
        assertEquals("Sea salt updated", viewModel.state.value.addSuccessMessage)
    }

    @Test
    fun `picking a shelf life sets a new expiry date`() = runTest {
        val item = viewModel.state.value.items.single()

        viewModel.updatePantryItem(item, "Salt", "1 kg", daysUntilExpiry = 7)

        assertEquals(LocalDate.now().plusDays(7).toString(), repository.lastUpdate?.expiryDate)
        assertEquals(7, viewModel.state.value.items.single().daysUntilExpiry)
    }

    @Test
    fun `non-numeric amounts are sent as the unit only`() = runTest {
        val item = viewModel.state.value.items.single()

        viewModel.updatePantryItem(item, "Coriander", "a bunch", daysUntilExpiry = null)

        assertNull(repository.lastUpdate?.quantity)
        assertEquals("a bunch", repository.lastUpdate?.unit)
    }

    @Test
    fun `failed update keeps the list and shows a message`() = runTest {
        repository.failUpdate = true
        val item = viewModel.state.value.items.single()

        viewModel.updatePantryItem(item, "Sea salt", "1 kg", daysUntilExpiry = null)

        assertEquals("Salt", viewModel.state.value.items.single().name)
        assertNull(viewModel.state.value.errorMessage)
        assertEquals("Server unavailable", viewModel.state.value.addSuccessMessage)
    }
}
