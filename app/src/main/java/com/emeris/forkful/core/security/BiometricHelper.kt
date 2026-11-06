package com.emeris.forkful.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.emeris.forkful.core.logging.ForkfulLogger

/**
 * PROG7314 - Mobile Application Development (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Represents the operational readiness state of device biometric sensors.
 */
enum class BiometricStatus {
    /** Biometric sensors (fingerprint/face) are enrolled and available */
    READY,
    /** Device does not have biometric sensor hardware */
    NO_HARDWARE,
    /** Device has hardware, but user has not enrolled fingerprints/face */
    NOT_ENROLLED,
    /** Sensor is temporarily busy, disabled, or locked out */
    UNAVAILABLE
}

/**
 * Helper utility encapsulating AndroidX BiometricPrompt API integration.
 * Provides hardware capability verification and secure authentication callbacks.
 */
object BiometricHelper {

    /**
     * Inspects device biometric capabilities against BIOMETRIC_STRONG and DEVICE_CREDENTIAL.
     *
     * @param context Application context
     * @return [BiometricStatus] indicating sensor readiness
     */
    fun checkStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    /**
     * Fast convenience check for whether biometric authentication can be requested.
     *
     * @param context Application context
     * @return True if [checkStatus] evaluates to [BiometricStatus.READY]
     */
    fun isAvailable(context: Context): Boolean {
        return checkStatus(context) == BiometricStatus.READY
    }

    /**
     * Prompts the user with a standard system biometric authentication dialog.
     * Dispatches callbacks on the main UI executor thread.
     *
     * @param activity FragmentActivity hosting the prompt lifecycle
     * @param title Title displayed on the biometric sheet
     * @param subtitle Explanatory prompt subtitle
     * @param onSuccess Invoked upon successful identity verification
     * @param onError Invoked upon fatal sensor failure or user cancellation
     * @param onFailed Invoked when a biometric sample is rejected (e.g. unrecognized finger)
     */
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
