package com.emeris.forkful.core.logging

import android.util.Log

/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Centralized logging utility for auditing screen lifecycle events,
 * user interactions, and network API communications across the app.
 */
object ForkfulLogger {
    private const val TAG = "ForkfulApp"

    /**
     * Logs composable and activity lifecycle transitions.
     *
     * @param screenName The name of the screen or composable
     * @param event The lifecycle event (e.g. ON_CREATE, ON_START, ON_STOP)
     */
    fun logLifecycle(screenName: String, event: String) {
        Log.i(TAG, "[LIFECYCLE] Screen: $screenName - Event: $event")
    }

    /**
     * Logs user actions, button taps, and biometric/session events.
     *
     * @param action Category or tag of the action
     * @param details Descriptive information about the event
     */
    fun logAction(action: String, details: String) {
        Log.d(TAG, "[USER_ACTION] $action - Details: $details")
    }

    /**
     * Logs network requests, endpoint invocations, and response outcomes.
     *
     * @param endpoint The network resource or table path
     * @param status HTTP or execution status
     */
    fun logNetwork(endpoint: String, status: String) {
        Log.d(TAG, "[NETWORK] Endpoint: $endpoint - Status: $status")
    }
}

//------------------------------End-Of-File------------------------------//
