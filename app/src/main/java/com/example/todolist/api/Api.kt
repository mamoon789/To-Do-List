package com.example.todolist.api

import com.example.todolist.models.ToDoResponse
import retrofit2.Response
import retrofit2.http.GET

interface Api
{
    @GET("c23f9391-ac23-46f6-bb44-e417e767c6c0")
    suspend fun getToDoList(): Response<ToDoResponse>
}