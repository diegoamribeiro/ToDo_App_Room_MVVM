package com.diegoribeiro.todoapp.data.repository

import androidx.lifecycle.LiveData
import com.diegoribeiro.todoapp.data.ToDoDao
import com.diegoribeiro.todoapp.data.models.ToDoData

class ToDoRepository(private val toDoDao: ToDoDao) {

    val getAllData: LiveData<List<ToDoData>> = toDoDao.getAllData()

    suspend fun insert(toDoData: ToDoData){
        toDoDao.insertData(toDoData)
    }

}