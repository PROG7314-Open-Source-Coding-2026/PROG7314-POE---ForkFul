package com.emeris.forkful.core.network

import com.emeris.forkful.core.session.TokenStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private class FakeTokenStore(
    var access: String? = "old-access",
    var refresh: String? = "old-refresh"
) : TokenStore {
    var expiresAt: Long? = null
    override fun getToken(): String? = access
    override fun getRefreshToken(): String? = refresh
    override fun updateTokens(token: String, refreshToken: String, expiresAt: Long?) {
        this.access = token
        this.refresh = refreshToken
        this.expiresAt = expiresAt
    }
}

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var store: FakeTokenStore
    private var rejected = false

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        store = FakeTokenStore()
        rejected = false
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun client(): OkHttpClient {
        val refreshApi = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(NetworkModule.json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthRefreshApi::class.java)
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer ${store.getToken()}")
                        .build()
                )
            }
            .authenticator(TokenAuthenticator(store, refreshApi) { rejected = true })
            .build()
    }

    private fun get(path: String) = client().newCall(Request.Builder().url(server.url(path)).build()).execute()

    @Test
    fun `expired token is refreshed and the call is retried`() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse().setBody(
                """{"token":"new-access","refreshToken":"new-refresh","expiresAt":1900000000,"userId":"u-1"}"""
            )
        )
        server.enqueue(MockResponse().setBody("[]"))

        val response = get("/functions/v1/pantry")

        assertEquals(200, response.code)
        assertEquals("new-access", store.access)
        assertEquals("new-refresh", store.refresh)
        assertEquals(1900000000L, store.expiresAt)
        server.takeRequest() //Original call
        val refreshCall = server.takeRequest()
        assertTrue(refreshCall.path!!.endsWith("/auth-refresh"))
        assertTrue(refreshCall.body.readUtf8().contains("old-refresh"))
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
        assertFalse(rejected)
    }

    @Test
    fun `rejected refresh reports the expired session`() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"Session expired"}"""))

        val response = get("/functions/v1/pantry")

        assertEquals(401, response.code)
        assertTrue(rejected)
        assertEquals("old-access", store.access)
    }

    @Test
    fun `missing refresh token reports the expired session`() {
        store.refresh = null
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get("/functions/v1/groceries")

        assertEquals(401, response.code)
        assertTrue(rejected)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `login failures never trigger a refresh`() {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"Invalid email or password"}"""))

        val response = get("/functions/v1/auth-sso")

        assertEquals(401, response.code)
        assertFalse(rejected)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `network failure during refresh keeps the user signed in`() {
        server.enqueue(MockResponse().setResponseCode(401))
        repeat(3) { server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)) }

        val response = get("/functions/v1/pantry")

        assertEquals(401, response.code)
        assertFalse(rejected)
        assertEquals("old-refresh", store.refresh)
    }
}
