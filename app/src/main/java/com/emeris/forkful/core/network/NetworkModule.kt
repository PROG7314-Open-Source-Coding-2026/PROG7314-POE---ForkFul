package com.emeris.forkful.core.network

import com.emeris.forkful.BuildConfig
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.session.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Interceptor that attaches the authenticated user's session JWT to every
 * request (NFR-04: HTTPS-only traffic, bearer-token auth).
 */
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

/**
 * Constructs the single Retrofit instance used by the app. The base URL is
 * the Supabase project URL configured in app/build.gradle.kts
 * (BuildConfig.SUPABASE_URL) - see SETUP.md.
 */
object NetworkModule {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        encodeDefaults = true
    }

    fun createApi(sessionManager: SessionManager): ForkfulApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL.trimEnd('/')}/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ForkfulApi::class.java)
    }
}
