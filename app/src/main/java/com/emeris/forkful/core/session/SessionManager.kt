package com.emeris.forkful.core.session

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

    //Onboarding flag
    fun isOnboarded(): Boolean = securePrefs.getBoolean(KEY_ONBOARDED, false)

    fun setOnboarded(onboarded: Boolean) {
        securePrefs.edit().putBoolean(KEY_ONBOARDED, onboarded).apply()
    }

    //DataStore prefs
    val prefsFlow: Flow<LocalPrefs> = context.prefsDataStore.data.map { prefs ->
        LocalPrefs(
            theme = prefs[KEY_THEME] ?: "light",
            language = prefs[KEY_LANGUAGE] ?: "en",
            notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
            biometricLockEnabled = prefs[KEY_BIOMETRIC] ?: false
        )
    }

    suspend fun updatePrefs(update: (LocalPrefs) -> LocalPrefs) {
        val current = LocalPrefs() //Default prefs
        context.prefsDataStore.edit { prefs ->
            val next = update(
                LocalPrefs(
                    theme = prefs[KEY_THEME] ?: current.theme,
                    language = prefs[KEY_LANGUAGE] ?: current.language,
                    notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
                    biometricLockEnabled = prefs[KEY_BIOMETRIC] ?: false
                )
            )
            prefs[KEY_THEME] = next.theme
            prefs[KEY_LANGUAGE] = next.language
            prefs[KEY_NOTIFICATIONS] = next.notificationsEnabled
            prefs[KEY_BIOMETRIC] = next.biometricLockEnabled
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

        val SESSION_KEYS = listOf(
            KEY_TOKEN, KEY_USER_ID, KEY_EMAIL, KEY_DISPLAY_NAME, KEY_REFRESH_TOKEN, KEY_EXPIRES_AT
        )

        val KEY_THEME = stringPreferencesKey("pref_theme")
        val KEY_LANGUAGE = stringPreferencesKey("pref_language")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("pref_notifications")
        val KEY_BIOMETRIC = booleanPreferencesKey("pref_biometric")
    }
}
