package com.emeris.forkful.ui.auth

/**
 * Forkful - Google Sign-In Credential Manager Helper
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Interacts with AndroidX Credential Manager to request Google ID tokens securely.
 */

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.emeris.forkful.BuildConfig
import com.emeris.forkful.core.logging.ForkfulLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

//Google credential result
sealed interface GoogleSignInResult {
    data class Success(val idToken: String, val email: String?, val displayName: String?) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failure(val message: String) : GoogleSignInResult
}

//Shared Credential Manager flow for Login and Sign Up
object GoogleSignInLauncher {

    suspend fun requestIdToken(activity: Activity?, logTag: String): GoogleSignInResult {
        if (activity == null) {
            ForkfulLogger.logAction(logTag, "Credential Manager needs an Activity context")
            return GoogleSignInResult.Failure("Google sign-in is unavailable right now. Please try again.")
        }

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(google.idToken, google.id, google.displayName)
            } else {
                ForkfulLogger.logAction(logTag, "Unexpected credential type: ${credential.type}")
                GoogleSignInResult.Failure("Google sign-in returned an unexpected account type.")
            }
        } catch (_: GetCredentialCancellationException) {
            ForkfulLogger.logAction(logTag, "Google sign-in cancelled by user")
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            ForkfulLogger.logAction(logTag, "No Google account available: ${e.message}")
            GoogleSignInResult.Failure("No Google account found on this device. Add one in your phone's settings and try again.")
        } catch (e: GetCredentialException) {
            ForkfulLogger.logAction(logTag, "Credential Manager error: ${e.type} ${e.message}")
            GoogleSignInResult.Failure("Google sign-in failed. Please check your connection and try again.")
        } catch (e: GoogleIdTokenParsingException) {
            ForkfulLogger.logAction(logTag, "Google token parse error: ${e.message}")
            GoogleSignInResult.Failure("Google sign-in failed. Please try again.")
        }
    }
}

//------------------------------End-Of-File------------------------------//
