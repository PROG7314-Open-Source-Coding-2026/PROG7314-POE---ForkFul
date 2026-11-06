package com.emeris.forkful

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.emeris.forkful.core.designsystem.ForkfulTheme
import com.emeris.forkful.core.i18n.LocalForkfulStrings
import com.emeris.forkful.core.i18n.getForkfulStrings
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.session.LocalPrefs
import com.emeris.forkful.ui.lock.AppLockScreen
import com.emeris.forkful.ui.navigation.ForkfulApp
import kotlinx.coroutines.launch

/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Primary single-activity entry point for Forkful.
 * Inherits from [FragmentActivity] to support the AndroidX [androidx.biometric.BiometricPrompt] lifecycle.
 * Manages cold-launch and background-resumption biometric security gating, theme preferences, and locale providers.
 */
class MainActivity : FragmentActivity() {

    /** Tracks whether the application transition was paused/stopped to enforce app locking on return */
    private var wasInBackground = false

    /** Holds the active biometric lock security overlay state */
    private val isAppLocked = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ForkfulLogger.logLifecycle("MainActivity", "ON_CREATE")

        val container = (application as ForkfulApplication).container
        val sessionManager = container.sessionManager
        val preferencesRepository = container.preferencesRepository

        // Immediately sync cloud preferences on cold start if a session exists
        if (sessionManager.getSession() != null) {
            lifecycleScope.launch {
                preferencesRepository.getPreferences()
            }
        }

        // Cold launch biometric lock check
        if (sessionManager.isBiometricLockEnabled() && sessionManager.getSession() != null) {
            isAppLocked.value = true
        }

        // Re-lock when returning from background
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                wasInBackground = true
            } else if (event == Lifecycle.Event.ON_START && wasInBackground) {
                wasInBackground = false
                if (sessionManager.isBiometricLockEnabled() && sessionManager.getSession() != null) {
                    isAppLocked.value = true
                }
            }
        })

        setContent {
            val prefs by sessionManager.prefsFlow.collectAsState(initial = sessionManager.getCachedPrefs())
            val isDarkTheme = when (prefs.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            ForkfulTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(
                    LocalForkfulStrings provides getForkfulStrings(prefs.language)
                ) {
                    if (isAppLocked.value && sessionManager.getSession() != null && sessionManager.isBiometricLockEnabled()) {
                        AppLockScreen(
                            onUnlocked = { isAppLocked.value = false },
                            onLogout = {
                                sessionManager.clearSession()
                                isAppLocked.value = false
                            }
                        )
                    } else {
                        key(sessionManager.getSession()?.userId) {
                            ForkfulApp()
                        }
                    }
                }
            }
        }
    }
}

//------------------------------End-Of-File------------------------------//
