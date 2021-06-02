package com.diegoribeiro.todoapp.data.repository

import androidx.lifecycle.LiveData
import com.diegoribeiro.todoapp.data.ToDoDao
import com.diegoribeiro.todoapp.data.models.ToDoData

class ToDoRepository(private val toDoDao: ToDoDao) {

    val getAllData: LiveData<List<ToDoData>> = toDoDao.getAllData()
    val sortByHighPriority: LiveData<List<ToDoData>> = toDoDao.sortByHighPriority()
    val sortByLowPriority: LiveData<List<ToDoData>> = toDoDao.sortByLowPriority()
    val sortByDateTime: LiveData<List<ToDoData>> = toDoDao.sortByDateTime()

    suspend fun deleteAll() =  toDoDao.deleteAll()

    suspend fun insert(toDoData: ToDoData){
        toDoDao.insertData(toDoData)
    }

    suspend fun updateData(toDoData: ToDoData){
        toDoDao.updateData(toDoData)
    }

    suspend fun deleteData(toDoData: ToDoData){
        toDoDao.deleteData(toDoData)
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>>{
        return toDoDao.searchDatabase(searchQuery)
    }

//    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>>{
//        return toDoDao.searchDatabase(searchQuery)
//    }


    
    

}