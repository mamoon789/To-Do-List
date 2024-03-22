package com.example.todolist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.todolist.repository.TODO_KEY
import com.example.todolist.models.ToDoResponse
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ToDoBroadcastReceiver : BroadcastReceiver()
{
    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onReceive(context: Context, intent: Intent)
    {
        /*
            Mark task completed.
         */

        CoroutineScope(Dispatchers.IO).launch {
            try
            {
                val id = intent.getIntExtra("id", 0)
                val key = stringPreferencesKey(TODO_KEY)
                val preferences = dataStore.data.first()
                val rawData = preferences[key]
                val data = Gson().fromJson(rawData, ToDoResponse::class.java)
                val todos = data.todos.toMutableList()
                todos.replaceAll {
                    if (it.id == id)
                    {
                        it.completed = true
                        it
                    } else it
                }
                data.todos = todos
                dataStore.edit { pref ->
                    pref[key] = Gson().toJson(data)
                }
            } catch (e: Exception)
            {
                Log.e("TAG", "onReceive: ${e.message}")
            }
        }
    }
}