package com.example.todolist.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.todolist.fragments.SortFragment
import com.example.todolist.models.Category
import com.example.todolist.models.ToDo
import com.example.todolist.repository.Resource
import com.example.todolist.repository.ToDoRepository
import com.example.todolist.viewmodel.Event
import com.example.todolist.viewmodel.ToDoViewModel
import com.nhaarman.mockitokotlin2.doReturn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ToDoViewModelTest
{
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: ToDoRepository

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    @Mock
    lateinit var eventUpdateSearchText : Event.UpdateSearchText

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup()
    {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        doReturn(MutableStateFlow("Cleaning").asStateFlow())
            .`when`(savedStateHandle).getStateFlow("searchText", "")

        doReturn(
            MutableStateFlow(listOf(Category("General", true))).asStateFlow()
        ).`when`(savedStateHandle).getStateFlow("categoryList", emptyList<Category>())

        doReturn(MutableStateFlow(SortFragment.SortOption.PRIORITY).asStateFlow())
            .`when`(savedStateHandle).getStateFlow("sortOption", SortFragment.SortOption.NONE)
    }

    @Test
    fun testOnEvent_GetTodoList_Successful() = runTest {
        val todoList = listOf(
            ToDo(1, "Cleaning", "General", "Room Cleaning", false, 1, "21-04-2024", "High"),
            ToDo(2, "Grocery", "General", "Do Grocery", false, 1, "21-04-2024", "High")
        )
        Mockito.`when`(repository.uiState)
            .thenReturn(MutableStateFlow(Resource.Success(todoList)).asStateFlow())

        val viewModel = ToDoViewModel(savedStateHandle, repository)
        viewModel.onEvent(Event.GetTodoList())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val resource = awaitItem()
            Assert.assertTrue(resource is Resource.Success && resource.data.size == 2)
            cancel()
        }
    }

    @Test
    fun testOnEvent_QueryTodoList_Successful() = runTest {
        val todoList = listOf(
            ToDo(1, "Cleaning", "General", "Room Cleaning", false, 1, "21-04-2024", "High"),
        )
        Mockito.`when`(repository.uiState)
            .thenReturn(MutableStateFlow(Resource.Success(todoList)).asStateFlow())

        val viewModel = ToDoViewModel(savedStateHandle, repository)
        viewModel.onEvent(Event.QueryTodoList())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val resource = awaitItem()
            Assert.assertTrue(resource is Resource.Success && resource.data.size == 1)
            cancel()
        }
    }

    @Test
    fun testOnEvent_UpdateSearchText_Successful() = runTest {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["searchText"] = ""

        Mockito.`when`(eventUpdateSearchText.searchText).thenReturn("Cleaning")

        val viewModel = ToDoViewModel(savedStateHandle, repository)

        launch {
            delay(100)
            viewModel.onEvent(eventUpdateSearchText)
        }

        viewModel.searchText.test {
            var currency = awaitItem()
            Assert.assertTrue(currency == "")
            currency = awaitItem()
            Assert.assertTrue(currency == "Cleaning")
            cancel()
        }
    }
}