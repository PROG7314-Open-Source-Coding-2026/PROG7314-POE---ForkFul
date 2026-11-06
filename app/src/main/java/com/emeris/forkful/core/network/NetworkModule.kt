package com.emeris.forkful.core.network

import com.emeris.forkful.BuildConfig
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.core.session.TokenStore
import com.emeris.forkful.data.remote.dto.RefreshTokenRequest
import com.emeris.forkful.data.remote.dto.RefreshTokenResponse
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

//Auth header interceptor
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getToken() ?: BuildConfig.SUPABASE_ANON_KEY
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .build()
        ForkfulLogger.logNetwork(request.url.encodedPath, "REQUEST ${request.method}")
        return chain.proceed(request)
    }
}

//Token refresh endpoint
interface AuthRefreshApi {
    @POST("functions/v1/auth-refresh")
    fun refresh(@Body body: RefreshTokenRequest): Call<RefreshTokenResponse>
}

//Refreshes an expired access token once, then retries the original call
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: AuthRefreshApi,
    private val onRefreshRejected: () -> Unit = {}
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.endsWith("/auth-sso") || path.endsWith("/auth-refresh")) return null
        if (response.priorResponse != null) return null //Already retried once

        synchronized(lock) {
            val currentToken = tokenStore.getToken() ?: return null //Signed out
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (sentToken != currentToken) {
                //Another call refreshed while this one was in flight
                return response.request.withBearer(currentToken)
            }

            val refreshToken = tokenStore.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                ForkfulLogger.logAction("SESSION", "401 with no refresh token stored")
                onRefreshRejected()
                return null
            }

            val refreshResponse = try {
                refreshApi.refresh(RefreshTokenRequest(refreshToken)).execute()
            } catch (e: IOException) {
                //Offline - keep the session so the user is not logged out
                ForkfulLogger.logNetwork("auth-refresh", "Refresh skipped, network error: ${e.message}")
                return null
            }

            val body = refreshResponse.body()
            if (!refreshResponse.isSuccessful || body == null) {
                ForkfulLogger.logNetwork("auth-refresh", "Refresh rejected (${refreshResponse.code()})")
                onRefreshRejected()
                return null
            }

            tokenStore.updateTokens(body.token, body.refreshToken, body.expiresAt)
            return response.request.withBearer(body.token)
        }
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()
}

//Retrofit factory
object NetworkModule {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        encodeDefaults = true
    }

    private val baseUrl: String get() = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/"

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    //Refresh calls use the anon key, never the expired user token
    private fun createRefreshApi(): AuthRefreshApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                        .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                        .build()
                )
            }
            .addInterceptor(loggingInterceptor())
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthRefreshApi::class.java)
    }

    fun createApi(
        sessionManager: SessionManager,
        onRefreshRejected: () -> Unit = {}
    ): ForkfulApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor())
            .authenticator(TokenAuthenticator(sessionManager, createRefreshApi(), onRefreshRejected))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ForkfulApi::class.java)
    }
}
