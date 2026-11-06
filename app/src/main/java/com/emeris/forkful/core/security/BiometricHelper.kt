package com.emeris.forkful.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.emeris.forkful.core.logging.ForkfulLogger

enum class BiometricStatus {
    READY,
    NO_HARDWARE,
    NOT_ENROLLED,
    UNAVAILABLE
}

object BiometricHelper {

    fun checkStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    fun isAvailable(context: Context): Boolean {
        return checkStatus(context) == BiometricStatus.READY
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Forkful",
        subtitle: String = "Verify your fingerprint or face to continue",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    ForkfulLogger.logAction("BIOMETRIC", "Authentication succeeded")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    ForkfulLogger.logAction("BIOMETRIC", "Authentication error $errorCode: $errString")
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    ForkfulLogger.logAction("BIOMETRIC", "Authentication failed")
                    onFailed()
                }
            }
        )

        prompt.authenticate(promptInfo)
    }
}

//------------------------------End-Of-File------------------------------//
