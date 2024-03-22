package com.example.todolist.comparator

import com.example.todolist.models.ToDo

object ToDoPriorityComparator: Comparator<ToDo>
{
    override fun compare(t: ToDo, t2: ToDo): Int
    {
        val p = when (t.priority.lowercase())
        {
            "high" -> 3
            "medium" -> 2
            "low" -> 1
            else -> 0
        }
        val p2 = when (t2.priority.lowercase())
        {
            "high" -> 3
            "medium" -> 2
            "low" -> 1
            else -> 0
        }
        return p2 - p
    }
}