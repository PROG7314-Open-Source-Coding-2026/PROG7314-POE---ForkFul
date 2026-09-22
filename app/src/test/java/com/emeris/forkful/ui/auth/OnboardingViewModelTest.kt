package com.emeris.forkful.ui.auth

import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences
import com.emeris.forkful.domain.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakePreferencesRepository(
    var failNext: Boolean = false
) : PreferencesRepository {

    var lastUpdate: PreferencesUpdate? = null
    var stored = UserPreferences(
        language = "en",
        dietaryTags = emptyList(),
        notificationsEnabled = true,
        theme = "light",
        onboarded = false,
        biometricLockEnabled = false
    )

    override suspend fun getPreferences(): Result<UserPreferences> = Result.success(stored)

    override suspend fun updatePreferences(update: PreferencesUpdate): Result<Boolean> {
        if (failNext) return Result.failure(IllegalStateException("network down"))
        lastUpdate = update
        return Result.success(true)
    }
}

class OnboardingViewModelTest {

    private lateinit var repository: FakePreferencesRepository
    private lateinit var viewModel: OnboardingViewModel

    @org.junit.Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakePreferencesRepository()
        viewModel = OnboardingViewModel(repository)
    }

    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `at least one craving is required to leave the cravings step`() {
        viewModel.nextStep()
        assertFalse(viewModel.state.value.canContinue)

        viewModel.toggleCuisine("italian")

        assertTrue(viewModel.state.value.canContinue)
    }

    @Test
    fun `toggling the same cuisine twice removes it`() {
        viewModel.toggleCuisine("italian")
        viewModel.toggleCuisine("italian")

        assertTrue(viewModel.state.value.selectedCuisines.isEmpty())
    }

    @Test
    fun `submit seeds cuisines, dietary tags and marks onboarding complete`() = runTest {
        viewModel.toggleCuisine("italian")
        viewModel.toggleCuisine("braai")
        viewModel.toggleDietary("Halaal")
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()

        viewModel.submit()

        val update = repository.lastUpdate
        assertEquals(setOf("italian", "braai"), update?.seedCuisines?.toSet())
        assertEquals(listOf("Halaal"), update?.dietaryTags)
        assertEquals(true, update?.onboarded)
        assertTrue(viewModel.state.value.isComplete)
    }

    @Test
    fun `submit failure surfaces an error and keeps the user onboarding`() = runTest {
        repository.failNext = true
        viewModel.toggleCuisine("sweets")
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()

        viewModel.submit()

        assertFalse(viewModel.state.value.isComplete)
        assertTrue(viewModel.state.value.errorMessage != null)
    }

    @Test
    fun `steps advance and retreat within bounds`() {
        assertEquals(0, viewModel.state.value.step)
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(3, viewModel.state.value.step)
        viewModel.previousStep()
        viewModel.previousStep()
        viewModel.previousStep()
        viewModel.previousStep()
        assertEquals(0, viewModel.state.value.step)
    }
}

class LoginViewModelValidationTest {

    @org.junit.Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank email never reaches the network layer`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository)

        viewModel.signInWithEmail("not-an-email", "secret1")

        assertTrue(viewModel.state.value is com.emeris.forkful.ui.auth.LoginUiState.Error)
        assertFalse(repository.lastCalled)
    }
}

class FakeAuthRepository : com.emeris.forkful.domain.repository.AuthRepository {
    var lastCalled = false

    override suspend fun signInWithGoogle(
        idToken: String,
        email: String?,
        displayName: String?
    ): Result<com.emeris.forkful.domain.repository.AuthOutcome> {
        lastCalled = true
        return Result.success(com.emeris.forkful.domain.repository.AuthOutcome(isNewUser = false))
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<com.emeris.forkful.domain.repository.AuthOutcome> {
        lastCalled = true
        return Result.success(com.emeris.forkful.domain.repository.AuthOutcome(isNewUser = false))
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<com.emeris.forkful.domain.repository.AuthOutcome> {
        lastCalled = true
        return Result.success(com.emeris.forkful.domain.repository.AuthOutcome(isNewUser = true))
    }

    override fun currentSession(): com.emeris.forkful.core.session.Session? = null

    override fun signOut() = Unit
}
