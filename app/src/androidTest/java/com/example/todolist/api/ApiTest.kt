package com.example.todolist.api

import com.example.todolist.api.Api
import com.example.todolist.models.ToDoResponse
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After


@HiltAndroidTest
class ApiTest
{
    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var api: Api

    @Before
    fun setup()
    {
        hiltAndroidRule.inject()
    }

    @Test
    fun testGetToDoList_Successful() = runTest {
        val mockResponse = MockResponse()
        val body = "{\n" +
                "  \"todos\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"Title\": \"Do something\",\n" +
                "      \"Category\": \"Personal\",\n" +
                "      \"todo\": \"Do something nice for someone I care about\",\n" +
                "      \"completed\": false,\n" +
                "      \"userId\": 26,\n" +
                "      \"date\": \"21-03-2024\",\n" +
                "      \"priority\": \"Low\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        mockResponse.setResponseCode(200)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        val response = api.getToDoList()
        mockWebServer.takeRequest()

        Assert.assertTrue(response.isSuccessful)
        Assert.assertTrue(response.body() != null)
        Assert.assertTrue(response.body() is ToDoResponse)
    }

    @After
    fun tearDown()
    {
        mockWebServer.shutdown()
    }
}