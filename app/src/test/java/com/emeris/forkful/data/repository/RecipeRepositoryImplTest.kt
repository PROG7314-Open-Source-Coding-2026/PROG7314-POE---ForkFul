package com.emeris.forkful.data.repository

import com.emeris.forkful.core.network.ForkfulApi
import com.emeris.forkful.core.network.NetworkModule
import com.emeris.forkful.domain.model.RecipeBoxFilter
import com.emeris.forkful.domain.model.SwipeDirection
import com.emeris.forkful.domain.repository.DeckQuery
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

//Recipe repo contract tests
class RecipeRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ForkfulApi
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkModule.json.let { json ->
            OkHttpClient().let { client ->
                Retrofit.Builder()
                    .baseUrl(server.url("/"))
                    .client(client)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()
                    .create(ForkfulApi::class.java)
            }
        }
        repository = RecipeRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getDeck parses the recipes-deck response`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                [
                  {
                    "recipeId": "r-1",
                    "title": "Margherita flatbread",
                    "imageUrl": "https://example.com/pizza.jpg",
                    "prepTimeMinutes": 20,
                    "tags": ["Tomato +18"],
                    "topTagPoints": 18,
                    "matchPercentage": 90,
                    "inPantryCount": 4,
                    "totalIngredientsCount": 5,
                    "rating": 4.9,
                    "cuisineType": "Italian"
                  }
                ]
                """.trimIndent()
            )
        )

        val deck = repository.getDeck(
            DeckQuery(mood = "italian", limit = 10)
        ).getOrThrow()

        assertEquals(1, deck.size)
        assertEquals("Margherita flatbread", deck.first().title)
        assertEquals(90, deck.first().matchPercentage)

        val request = server.takeRequest()
        assertEquals("/functions/v1/recipes-deck?mood=italian&limit=10", request.path)
    }

    @Test
    fun `recordSwipe posts the exact contract body`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "status": "recorded",
                  "swipeId": "s-77",
                  "updatedTagPoints": [
                    { "moodProfileKey": "italian", "tagName": "Tomato", "points": 18 }
                  ]
                }
                """.trimIndent()
            )
        )

        val outcome = repository.recordSwipe("r-1", SwipeDirection.RIGHT, "italian").getOrThrow()

        assertEquals("s-77", outcome.swipeId)
        assertEquals(1, outcome.updatedTagPoints.size)
        assertEquals(18, outcome.updatedTagPoints.first().points)

        val request = server.takeRequest()
        assertEquals("/functions/v1/swipes", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"recipeId\":\"r-1\""))
        assertTrue(body.contains("\"moodProfileId\":\"italian\""))
        assertTrue(body.contains("\"direction\":\"RIGHT\""))
    }

    @Test
    fun `getRecipeBox filters by status wire name`() = runTest {
        server.enqueue(MockResponse().setBody("[]"))

        repository.getRecipeBox(RecipeBoxFilter.COOKED).getOrThrow()

        val request = server.takeRequest()
        assertEquals("/functions/v1/recipe-box?status=COOKED", request.path)
    }

    @Test
    fun `setCooked posts the mark-cooked body`() = runTest {
        server.enqueue(MockResponse().setBody("""{ "status": "ok" }"""))

        val result = repository.setCooked("r-3", cooked = true)

        assertTrue(result.getOrThrow())
        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("\"recipeId\":\"r-3\""))
        assertTrue(body.contains("\"cooked\":true"))
    }
}
