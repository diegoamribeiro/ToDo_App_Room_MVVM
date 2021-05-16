package com.diegoribeiro.todoapp.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "todo_table")
@Parcelize
data class ToDoData(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        var title: String,
        var priority: Priority,
        var description: String,
        var deadline: String
): Parcelable