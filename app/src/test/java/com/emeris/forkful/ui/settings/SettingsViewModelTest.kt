package com.emeris.forkful.ui.settings

import com.emeris.forkful.core.session.Session
import com.emeris.forkful.domain.model.PreferencesUpdate
import com.emeris.forkful.domain.model.UserPreferences
import com.emeris.forkful.domain.repository.AuthOutcome
import com.emeris.forkful.domain.repository.AuthRepository
import com.emeris.forkful.domain.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeAuthRepository : AuthRepository {
    var signedOut = false

    override suspend fun signInWithGoogle(idToken: String, email: String?, displayName: String?): Result<AuthOutcome> =
        Result.success(AuthOutcome(isNewUser = false, needsOnboarding = false))

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthOutcome> =
        Result.success(AuthOutcome(isNewUser = false, needsOnboarding = false))

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<AuthOutcome> =
        Result.success(AuthOutcome(isNewUser = true, needsOnboarding = true))

    override fun currentSession(): Session? = null

    override fun signOut() {
        signedOut = true
    }
}

private class FakeSettingsPreferencesRepository : PreferencesRepository {
    var lastUpdate: PreferencesUpdate? = null
    var stored = UserPreferences(
        language = "en",
        dietaryTags = emptyList(),
        notificationsEnabled = true,
        theme = "light",
        onboarded = true,
        biometricLockEnabled = false
    )

    override suspend fun getPreferences(): Result<UserPreferences> = Result.success(stored)

    override suspend fun updatePreferences(update: PreferencesUpdate): Result<Boolean> {
        lastUpdate = update
        stored = stored.copy(
            biometricLockEnabled = update.biometricLockEnabled ?: stored.biometricLockEnabled,
            theme = update.theme ?: stored.theme,
            language = update.language ?: stored.language,
            notificationsEnabled = update.notificationsEnabled ?: stored.notificationsEnabled
        )
        return Result.success(true)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var preferencesRepository: FakeSettingsPreferencesRepository
    private lateinit var authRepository: FakeAuthRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = FakeSettingsPreferencesRepository()
        authRepository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `enabling biometric lock persists update and reflects in state`() = runTest {
        val viewModel = SettingsViewModel(preferencesRepository, authRepository)

        assertFalse(viewModel.state.value.preferences.biometricLockEnabled)

        viewModel.setBiometricLock(true)

        assertEquals(true, preferencesRepository.lastUpdate?.biometricLockEnabled)
        assertTrue(viewModel.state.value.preferences.biometricLockEnabled)
    }

    @Test
    fun `disabling biometric lock updates repository and state`() = runTest {
        preferencesRepository.stored = preferencesRepository.stored.copy(biometricLockEnabled = true)
        val viewModel = SettingsViewModel(preferencesRepository, authRepository)

        assertTrue(viewModel.state.value.preferences.biometricLockEnabled)

        viewModel.setBiometricLock(false)

        assertEquals(false, preferencesRepository.lastUpdate?.biometricLockEnabled)
        assertFalse(viewModel.state.value.preferences.biometricLockEnabled)
    }

    @Test
    fun `logout triggers authRepository signOut`() = runTest {
        val viewModel = SettingsViewModel(preferencesRepository, authRepository)

        viewModel.logout()

        assertTrue(authRepository.signedOut)
        assertTrue(viewModel.state.value.loggedOut)
    }
}

//------------------------------End-Of-File------------------------------//
