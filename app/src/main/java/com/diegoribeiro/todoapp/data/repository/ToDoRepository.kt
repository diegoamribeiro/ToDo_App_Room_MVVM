package com.diegoribeiro.todoapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.diegoribeiro.todoapp.data.ToDoDao
import com.diegoribeiro.todoapp.data.models.ToDoData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ToDoRepository @Inject constructor(
    private val toDoDao: ToDoDao,
    @ApplicationContext private val context: Context
) {

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

    fun updateTimeReviewDialog() {
        val settings: SharedPreferences = context.getSharedPreferences(CONFIGURATION, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putLong(SHOW_REVIEW_DIALOG, System.currentTimeMillis())
        editor.apply()
    }

    fun shouldShowFeedbackDialog(): Boolean {
        val sharedPreferences = context.getSharedPreferences(CONFIGURATION, Context.MODE_PRIVATE)
        val lastShownTime = sharedPreferences.getLong(SHOW_REVIEW_DIALOG, 0)
        val currentTime = System.currentTimeMillis()
        val daysPassed = TimeUnit.MILLISECONDS.toDays(currentTime - lastShownTime)
        return daysPassed >= 0
    }

    companion object {
        private const val CONFIGURATION = "configuration"
        private const val SHOW_REVIEW_DIALOG = "show_review_dialog"
    }


    
    

}