package com.example.todolist.models

data class ToDo(
    var id: Int,
    var Title: String,
    var Category: String,
    var todo: String,
    var completed: Boolean = false,
    var userId: Int,
    var date: String,
    var priority: String
)
