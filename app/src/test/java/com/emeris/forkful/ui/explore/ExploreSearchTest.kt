package com.emeris.forkful.ui.explore

import com.emeris.forkful.ui.swipestack.FakeRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreSearchTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRecipeRepository
    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeRecipeRepository()
        viewModel = ExploreViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typed text shows immediately while the search waits for the debounce`() = runTest(dispatcher) {
        runCurrent()

        viewModel.onSearchInput("bob")

        assertEquals("bob", viewModel.state.value.searchText)
        assertEquals("", viewModel.state.value.searchQuery)

        advanceTimeBy(400)
        runCurrent()

        assertEquals("bob", viewModel.state.value.searchQuery)
        assertEquals("bob", repository.lastDeckQuery?.search)
    }

    @Test
    fun `clear filters empties the search box`() = runTest(dispatcher) {
        viewModel.onSearchInput("pasta")
        advanceTimeBy(400)
        runCurrent()

        viewModel.clearFilters()
        runCurrent()

        assertEquals("", viewModel.state.value.searchText)
        assertEquals("", viewModel.state.value.searchQuery)
    }
}
