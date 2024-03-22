package com.example.todolist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.models.Category
import com.example.todolist.models.ToDo
import com.example.todolist.repository.ToDoRepository
import com.example.todolist.fragments.SortFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ToDoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, private val repository: ToDoRepository
) : ViewModel()
{
    val uiState = repository.uiState
    val uiState2 = repository.uiState2

    /*
        Used SavedStateHandle to restore UI state on process death.
     */

    val todoList = savedStateHandle.getStateFlow("todoList", emptyList<ToDo>())
    val categoryList = savedStateHandle.getStateFlow("categoryList", emptyList<Category>())
    val searchText = savedStateHandle.getStateFlow("searchText", "")
    val sortOption = savedStateHandle.getStateFlow("sortOption", SortFragment.SortOption.NONE)

    val todoId = savedStateHandle.getStateFlow("todoId", 0)
    val todoTitle = savedStateHandle.getStateFlow("todoTitle", "")
    val todoCategory = savedStateHandle.getStateFlow("todoCategory", "")
    val todoTodo = savedStateHandle.getStateFlow("todoTodo", "")
    val todoCompleted = savedStateHandle.getStateFlow("todoCompleted", false)
    val todoUserId = savedStateHandle.getStateFlow("todoUserId", 0)
    val todoDate = savedStateHandle.getStateFlow("todoDate", "")
    val todoPriority = savedStateHandle.getStateFlow("todoPriority", "")

    init
    {
        onEvent(Event.GetTodoList())
    }

    fun onEvent(event: Event)
    {
        when (event)
        {
            is Event.GetTodoList -> viewModelScope.launch {
                repository.getTodoList()
            }

            is Event.QueryTodoList -> viewModelScope.launch {
                repository.queryTodoList(
                    searchText.value,
                    categoryList.value.filter { it.isChecked }.map { it.name },
                    sortOption.value
                )
            }

            is Event.AddTodo -> viewModelScope.launch {
                repository.addTodo(
                    ToDo(
                        Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE),
                        todoTitle.value,
                        todoCategory.value,
                        todoTodo.value,
                        todoCompleted.value,
                        todoUserId.value,
                        todoDate.value,
                        todoPriority.value
                    )
                )
            }

            is Event.UpdateTodo -> viewModelScope.launch {

                event.todo?.let {
                    repository.updateTodoPrefs(it)
                } ?: repository.updateTodo(
                    ToDo(
                        todoId.value,
                        todoTitle.value,
                        todoCategory.value,
                        todoTodo.value,
                        todoCompleted.value,
                        todoUserId.value,
                        todoDate.value,
                        todoPriority.value
                    )
                )
            }

            is Event.DeleteTodo -> viewModelScope.launch {
                repository.deleteTodo(todoId.value)
            }

            is Event.UpdateTodoList -> savedStateHandle["todoList"] = event.todoList
            is Event.UpdateCategoryList -> savedStateHandle["categoryList"] = event.categoryList
            is Event.UpdateSearchText -> savedStateHandle["searchText"] = event.searchText
            is Event.UpdateSortOption -> savedStateHandle["sortOption"] = event.sortOption

            is Event.UpdateTodoId -> savedStateHandle["todoId"] = event.todoId
            is Event.UpdateTodoTitle -> savedStateHandle["todoTitle"] = event.todoTitle
            is Event.UpdateTodoCategory -> savedStateHandle["todoCategory"] = event.todoCategory
            is Event.UpdateTodoTodo -> savedStateHandle["todoTodo"] = event.todoTodo
            is Event.UpdateTodoCompleted -> savedStateHandle["todoCompleted"] = event.todoCompleted
            is Event.UpdateTodoUserId -> savedStateHandle["todoUserId"] = event.todoUserId
            is Event.UpdateTodoDate -> savedStateHandle["todoDate"] = event.todoDate
            is Event.UpdateTodoPriority -> savedStateHandle["todoPriority"] = event.todoPriority
        }
    }
}

/*
    Class to communicate with viewmodel.
 */
sealed class Event
{
    class GetTodoList : Event()
    class QueryTodoList : Event()
    class AddTodo : Event()
    class UpdateTodo(var todo: ToDo? = null) : Event()
    class DeleteTodo : Event()

    class UpdateTodoList(val todoList: List<ToDo>) : Event()
    class UpdateCategoryList(val categoryList: List<Category>) : Event()
    class UpdateSearchText(val searchText: String) : Event()
    class UpdateSortOption(val sortOption: SortFragment.SortOption) : Event()

    class UpdateTodoId(val todoId: Int) : Event()
    class UpdateTodoTitle(val todoTitle: String) : Event()
    class UpdateTodoCategory(val todoCategory: String) : Event()
    class UpdateTodoTodo(val todoTodo: String) : Event()
    class UpdateTodoCompleted(val todoCompleted: Boolean) : Event()
    class UpdateTodoUserId(val todoUserId: Int) : Event()
    class UpdateTodoDate(val todoDate: String) : Event()
    class UpdateTodoPriority(val todoPriority: String) : Event()
}