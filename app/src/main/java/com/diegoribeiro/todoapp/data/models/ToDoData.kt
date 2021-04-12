package com.diegoribeiro.todoapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class ToDoData(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val title: String,
        val priority: Priority,
        val description: String
) {
}