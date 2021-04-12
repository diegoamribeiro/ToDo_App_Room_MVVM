package com.diegoribeiro.todoapp.data.converter

import androidx.room.TypeConverter
import com.diegoribeiro.todoapp.data.models.Priority

class Converter {
    @TypeConverter
    fun fromPriority(priority: Priority): String{
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }
}