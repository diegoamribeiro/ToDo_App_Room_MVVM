package com.diegoribeiro.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diegoribeiro.todoapp.data.converter.Converter
import com.diegoribeiro.todoapp.data.models.ToDoData

@Database(entities = [ToDoData::class], version = 2, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun toDoDao(): ToDoDao
}