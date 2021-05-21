package com.diegoribeiro.todoapp.data.viewmodel

import android.app.Application
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.Priority
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class SharedViewModel(application: Application) : AndroidViewModel(application){

    val emptyDatabase: MutableLiveData<Boolean> = MutableLiveData(true)

    fun verifyEmptyList(list: List<ToDoData>){
        emptyDatabase.value = list.isEmpty()
    }

    val listener: AdapterView.OnItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(position){
                0 -> {(parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(application, R.color.red))}
                1 -> {(parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(application, R.color.yellow))}
                2 -> {(parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(application, R.color.green))}
            }
        }
    }

    fun parseIntToPriority(priority: Int): Priority {

        return when(priority){
            0 -> {Priority.HIGH}
            1 -> {Priority.MEDIUM}
            2 -> {Priority.LOW}
            else -> Priority.LOW
        }
    }

    fun parsePriorityToInt(priority: Priority): Int {
        return when (priority) {
            Priority.HIGH -> 0
            Priority.MEDIUM -> 1
            Priority.LOW -> 2
        }
    }

//    fun getTitle(title: ToDoData){
//        _liveTitle!!.value = title
//    }


    fun setDeadLine(dateTime: ToDoDateTime): OffsetDateTime{
        return OffsetDateTime.of(
            dateTime.year,
            dateTime.month,
            dateTime.day,
            dateTime.hour,
            dateTime.minute,
            0,
            0,
            OffsetDateTime.now().offset
        )
    }

    fun verifyDataFromUser(title: String, description: String, date: String, time: String): Boolean{
        return !(title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty())
    }
}