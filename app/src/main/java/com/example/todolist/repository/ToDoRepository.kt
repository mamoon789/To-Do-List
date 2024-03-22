package com.example.todolist.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.todolist.api.Api
import com.example.todolist.comparator.ToDoPriorityComparator
import com.example.todolist.fragments.SortFragment
import com.example.todolist.models.Error
import com.example.todolist.models.ToDo
import com.example.todolist.models.ToDoResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

const val TODO_KEY = "todo_key"

@Suppress("NAME_SHADOWING")
class ToDoRepository @Inject constructor(
    private val api: Api, private val dataStore: DataStore<Preferences>
)
{
    /*
        Screen 1 State
     */

    private val _uiState: MutableStateFlow<Resource> = MutableStateFlow(Resource.Loading())
    val uiState = _uiState.asStateFlow()

    /*
        Screen 2 State
     */

    private val _uiState2: MutableSharedFlow<Resource> = MutableSharedFlow()
    val uiState2 = _uiState2.asSharedFlow()

    suspend fun getTodoList()
    {
        _uiState.emit(try
        {
            val key = stringPreferencesKey(TODO_KEY)
            val preferences = dataStore.data.first()
            val rawData = preferences[key]

            if (rawData != null)
            {
                val data = Gson().fromJson(rawData, ToDoResponse::class.java)
                Resource.Success(data.todos)
            } else
            {
                val response = api.getToDoList()
                val data = response.body()
                if (response.isSuccessful && data != null)
                {
                    dataStore.edit { preferences ->
                        preferences[key] = Gson().toJson(data)
                    }
                    Resource.Success(data.todos)
                } else
                {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val error = Gson().fromJson(errorBody, Error::class.java)
                    Resource.Error(
                        error?.message ?: "Something is wrong!",
                        error?.description ?: "${response.code()} ${response.message()}"
                    )
                }
            }
        } catch (e: IOException)
        {
            Resource.Error("Something is wrong!", e.message ?: "Check your internet connection")
        } catch (e: Exception)
        {
            Resource.Error("Something is wrong!", e.message ?: "Try later")
        })
    }

    suspend fun queryTodoList(
        title: String, pickedCategoryList: List<String>, sortOption: SortFragment.SortOption
    )
    {
        _uiState.emit(try
        {
            val key = stringPreferencesKey(TODO_KEY)
            val preferences = dataStore.data.first()
            val rawData = preferences[key]
            val data = Gson().fromJson(rawData, ToDoResponse::class.java)
            var todos = data.todos.filter { pickedCategoryList.contains(it.Category) }
            todos = if (title.isEmpty())
            {
                todos
            } else
            {
                todos.filter { it.Title.contains(title, true) }
            }
            Resource.Success(when (sortOption)
            {
                SortFragment.SortOption.PRIORITY -> todos.sortedWith(ToDoPriorityComparator)
                SortFragment.SortOption.DATE -> todos.sortedBy { it.date }
                SortFragment.SortOption.COMPLETED -> todos.sortedByDescending { it.completed }
                SortFragment.SortOption.NONE -> todos
            })
        } catch (e: Exception)
        {
            Resource.Error("Something is wrong!", e.message ?: "Try later")
        })
    }

    suspend fun addTodo(toDo: ToDo)
    {
        _uiState2.emit(Resource.Loading())
        _uiState2.emit(
            try
            {
                if (toDo.run { Title.isEmpty() || todo.isEmpty() || date.isEmpty() })
                {
                    Resource.Error("Fill all fields to proceed", "All fields are mandotary")
                } else
                {
                    val key = stringPreferencesKey(TODO_KEY)
                    val preferences = dataStore.data.first()
                    val rawData = preferences[key]
                    val data = Gson().fromJson(rawData, ToDoResponse::class.java)
                    val todos = data.todos.toMutableList()
                    todos.add(toDo)
                    data.todos = todos
                    dataStore.edit { preferences ->
                        preferences[key] = Gson().toJson(data)
                    }
                    Resource.Success(data.todos)
                }
            } catch (e: Exception)
            {
                Resource.Error("Something is wrong!", e.message ?: "Try later")
            }
        )
    }

    suspend fun updateTodo(toDo: ToDo)
    {
        _uiState2.emit(Resource.Loading())
        _uiState2.emit(
            try
            {
                if (toDo.run { Title.isEmpty() || todo.isEmpty() || date.isEmpty() })
                {
                    Resource.Error("Fill all fields to proceed", "All fields are mandotary")
                } else
                {
                    val key = stringPreferencesKey(TODO_KEY)
                    val preferences = dataStore.data.first()
                    val rawData = preferences[key]
                    val data = Gson().fromJson(rawData, ToDoResponse::class.java)
                    val todos = data.todos.toMutableList()
                    todos.replaceAll { if (it.id == toDo.id) toDo else it }
                    data.todos = todos
                    dataStore.edit { preferences ->
                        preferences[key] = Gson().toJson(data)
                    }
                    Resource.Success(data.todos)
                }
            } catch (e: Exception)
            {
                Resource.Error("Something is wrong!", e.message ?: "Try later")
            }
        )
    }

    suspend fun updateTodoPrefs(toDo: ToDo)
    {
        /*
            Updating task completed.
            No need to return as the UI is up-to date.
         */

        try
        {
            val key = stringPreferencesKey(TODO_KEY)
            val preferences = dataStore.data.first()
            val rawData = preferences[key]
            val data = Gson().fromJson(rawData, ToDoResponse::class.java)
            val todos = data.todos.toMutableList()
            todos.replaceAll { if (it.id == toDo.id) toDo else it }
            data.todos = todos
            dataStore.edit { preferences ->
                preferences[key] = Gson().toJson(data)
            }
        } catch (e: Exception)
        {
            Log.d("TAG", "updateTodoCompleted: ${e.message}")
        }
    }

    suspend fun deleteTodo(id: Int)
    {
        _uiState2.emit(Resource.Loading())
        _uiState2.emit(
            try
            {
                val key = stringPreferencesKey(TODO_KEY)
                val preferences = dataStore.data.first()
                val rawData = preferences[key]
                val data = Gson().fromJson(rawData, ToDoResponse::class.java)
                val todos = data.todos.toMutableList()
                todos.removeAll { it.id == id }
                data.todos = todos
                dataStore.edit { preferences ->
                    preferences[key] = Gson().toJson(data)
                }
                Resource.Success(data.todos)
            } catch (e: Exception)
            {
                Resource.Error("Something is wrong!", e.message ?: "Try later")
            }
        )
    }
}

/*
    Class to wrap response for different UI states.
 */

sealed class Resource
{
    class Success(var data: List<ToDo>) : Resource()
    class Error(var message: String, var description: String, var code: Int? = 0) : Resource()
    class Loading : Resource()
}