package com.emeris.forkful.core.session

/**
 * Forkful - Secure Session Storage & Preferences DataStore
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Encrypts JWT auth tokens via Android Jetpack EncryptedSharedPreferences (AES256) and manages DataStore preferences.
 */

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.emeris.forkful.core.logging.ForkfulLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

//Auth session model
data class Session(
    val token: String,
    val userId: String,
    val email: String?,
    val displayName: String?,
    val refreshToken: String? = null,
    val expiresAt: Long? = null //Unix seconds
)

//App preferences model
data class LocalPrefs(
    val theme: String = "light",
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = false
)

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "forkful_prefs"
)

//Token access used by the network layer
interface TokenStore {
    fun getToken(): String?
    fun getRefreshToken(): String?
    fun updateTokens(token: String, refreshToken: String, expiresAt: Long?)
}

//Session & prefs store
class SessionManager(private val context: Context) : TokenStore {

    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "forkful_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    //Session methods
    override fun getToken(): String? = securePrefs.getString(KEY_TOKEN, null)

    fun getSession(): Session? {
        val token = securePrefs.getString(KEY_TOKEN, null) ?: return null
        val userId = securePrefs.getString(KEY_USER_ID, null) ?: return null
        return Session(
            token = token,
            userId = userId,
            email = securePrefs.getString(KEY_EMAIL, null),
            displayName = securePrefs.getString(KEY_DISPLAY_NAME, null),
            refreshToken = securePrefs.getString(KEY_REFRESH_TOKEN, null),
            expiresAt = securePrefs.getLong(KEY_EXPIRES_AT, 0L).takeIf { it > 0L }
        )
    }

    override fun getRefreshToken(): String? = securePrefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveSession(session: Session) {
        securePrefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_EXPIRES_AT, session.expiresAt ?: 0L)
            .apply()
        ForkfulLogger.logAction("SESSION", "Session saved for user ${session.userId}")
    }

    //Swap in refreshed tokens
    override fun updateTokens(token: String, refreshToken: String, expiresAt: Long?) {
        securePrefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt ?: 0L)
            .commit()
        ForkfulLogger.logAction("SESSION", "Access token refreshed")
    }

    //Clears credentials only - the onboarding flag survives logout
    fun clearSession() {
        securePrefs.edit().apply {
            SESSION_KEYS.forEach { remove(it) }
        }.apply()
        ForkfulLogger.logAction("SESSION", "Session cleared (logout)")
    }

    //Expired-session event: the app listens and routes back to Login
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    @Volatile
    private var pendingLoginMessage: String? = null

    fun expireSession() {
        if (getToken() == null) return
        clearSession()
        pendingLoginMessage = "Your session has expired. Please sign in again."
        ForkfulLogger.logAction("SESSION", "Session expired - routing to login")
        _sessionExpired.tryEmit(Unit)
    }

    //One-shot message for the Login screen
    fun consumeLoginMessage(): String? = pendingLoginMessage.also { pendingLoginMessage = null }

    //Debug-build helpers for testing expiry on a device
    fun debugBreakAccessToken() {
        securePrefs.edit().putString(KEY_TOKEN, "expired-access-token").commit()
    }

    fun debugBreakAllTokens() {
        securePrefs.edit()
            .putString(KEY_TOKEN, "expired-access-token")
            .putString(KEY_REFRESH_TOKEN, "revoked-refresh-token")
            .commit()
    }

    //Onboarding flag
    fun isOnboarded(): Boolean = securePrefs.getBoolean(KEY_ONBOARDED, false)

    fun setOnboarded(onboarded: Boolean) {
        securePrefs.edit().putBoolean(KEY_ONBOARDED, onboarded).apply()
    }

    //Biometric lock fast synchronous check
    fun isBiometricLockEnabled(): Boolean = securePrefs.getBoolean(KEY_BIOMETRIC_LOCK, false)

    fun setBiometricLockEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
    }

    // Fast synchronous cache for cold-start launch without UI flicker
    fun getCachedPrefs(): LocalPrefs {
        val fastTheme = securePrefs.getString(KEY_THEME_FAST, null) ?: "light"
        val fastLang = securePrefs.getString(KEY_LANGUAGE_FAST, null) ?: "en"
        val fastNotif = securePrefs.getBoolean(KEY_NOTIFICATIONS_FAST, true)
        val fastBio = securePrefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
        return LocalPrefs(
            theme = fastTheme,
            language = fastLang,
            notificationsEnabled = fastNotif,
            biometricLockEnabled = fastBio
        )
    }

    //DataStore prefs
    val prefsFlow: Flow<LocalPrefs> = context.prefsDataStore.data.map { prefs ->
        val current = getCachedPrefs()
        val theme = prefs[KEY_THEME] ?: current.theme
        val language = prefs[KEY_LANGUAGE] ?: current.language
        val notifications = prefs[KEY_NOTIFICATIONS] ?: current.notificationsEnabled
        val biometric = prefs[KEY_BIOMETRIC] ?: current.biometricLockEnabled

        // Keep fast cache synchronised with DataStore disk emissions
        securePrefs.edit()
            .putString(KEY_THEME_FAST, theme)
            .putString(KEY_LANGUAGE_FAST, language)
            .putBoolean(KEY_NOTIFICATIONS_FAST, notifications)
            .putBoolean(KEY_BIOMETRIC_LOCK, biometric)
            .apply()

        LocalPrefs(
            theme = theme,
            language = language,
            notificationsEnabled = notifications,
            biometricLockEnabled = biometric
        )
    }

    suspend fun updatePrefs(update: (LocalPrefs) -> LocalPrefs) {
        val current = getCachedPrefs()
        context.prefsDataStore.edit { prefs ->
            val next = update(
                LocalPrefs(
                    theme = prefs[KEY_THEME] ?: current.theme,
                    language = prefs[KEY_LANGUAGE] ?: current.language,
                    notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: current.notificationsEnabled,
                    biometricLockEnabled = prefs[KEY_BIOMETRIC] ?: current.biometricLockEnabled
                )
            )
            prefs[KEY_THEME] = next.theme
            prefs[KEY_LANGUAGE] = next.language
            prefs[KEY_NOTIFICATIONS] = next.notificationsEnabled
            prefs[KEY_BIOMETRIC] = next.biometricLockEnabled
            setBiometricLockEnabled(next.biometricLockEnabled)

            // Immediately persist fast cache so synchronous lookups never flash old values
            securePrefs.edit()
                .putString(KEY_THEME_FAST, next.theme)
                .putString(KEY_LANGUAGE_FAST, next.language)
                .putBoolean(KEY_NOTIFICATIONS_FAST, next.notificationsEnabled)
                .putBoolean(KEY_BIOMETRIC_LOCK, next.biometricLockEnabled)
                .apply()
        }
    }

    private companion object {
        const val KEY_TOKEN = "session_token"
        const val KEY_USER_ID = "session_user_id"
        const val KEY_EMAIL = "session_email"
        const val KEY_DISPLAY_NAME = "session_display_name"
        const val KEY_REFRESH_TOKEN = "session_refresh_token"
        const val KEY_EXPIRES_AT = "session_expires_at"
        const val KEY_ONBOARDED = "session_onboarded"
        const val KEY_BIOMETRIC_LOCK = "session_biometric_lock"
        const val KEY_THEME_FAST = "session_theme_fast"
        const val KEY_LANGUAGE_FAST = "session_language_fast"
        const val KEY_NOTIFICATIONS_FAST = "session_notifications_fast"

        val SESSION_KEYS = listOf(
            KEY_TOKEN, KEY_USER_ID, KEY_EMAIL, KEY_DISPLAY_NAME, KEY_REFRESH_TOKEN, KEY_EXPIRES_AT
        )

        val KEY_THEME = stringPreferencesKey("pref_theme")
        val KEY_LANGUAGE = stringPreferencesKey("pref_language")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("pref_notifications")
        val KEY_BIOMETRIC = booleanPreferencesKey("pref_biometric")
    }
}

//------------------------------End-Of-File------------------------------//
