package com.emeris.forkful.core.logging

import android.util.Log

object ForkfulLogger {
    private const val TAG = "ForkfulApp"

    fun logLifecycle(screenName: String, event: String) {
        Log.i(TAG, "[LIFECYCLE] Screen: $screenName - Event: $event")
    }

    fun logAction(action: String, details: String) {
        Log.d(TAG, "[USER_ACTION] $action - Details: $details")
    }

    fun logNetwork(endpoint: String, status: String) {
        Log.d(TAG, "[NETWORK] Endpoint: $endpoint - Status: $status")
    }
}