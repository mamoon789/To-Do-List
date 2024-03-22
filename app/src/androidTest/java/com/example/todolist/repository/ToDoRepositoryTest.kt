package com.example.todolist.repository

import com.example.todolist.repository.Resource
import com.example.todolist.repository.ToDoRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ToDoRepositoryTest
{
    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: ToDoRepository

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Before
    fun setup()
    {
        hiltAndroidRule.inject()
    }

    @Test
    fun testGetToDoList_Successful_Api() = runTest {
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

        repository.getTodoList()
        mockWebServer.takeRequest()

        val resource = repository.uiState.value
        Assert.assertTrue(resource is Resource.Success && resource.data.size == 1)
    }

    @After
    fun tearDown()
    {
        mockWebServer.shutdown()
    }
}