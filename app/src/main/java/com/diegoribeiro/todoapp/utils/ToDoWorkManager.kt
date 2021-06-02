package com.diegoribeiro.todoapp.utils

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.ToDoConstants
import com.diegoribeiro.todoapp.data.models.Priority
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import kotlinx.android.synthetic.main.fragment_add.view.*
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
class ToDoWorkManager(val workManager: WorkManager) {

    private var deadLine: ToDoDateTime = ToDoDateTime()

    fun createWorkManager(toDoData: ToDoData, view: View){
        if (toDoData.dateTime!! > OffsetDateTime.now()){
            deadLine = ToDoDateTime(
                toDoData.dateTime!!.dayOfMonth,
                toDoData.dateTime!!.monthValue,
                toDoData.dateTime!!.year,
                toDoData.dateTime!!.hour,
                toDoData.dateTime!!.minute
            )

            val timeTilFuture = ChronoUnit.MILLIS.between(
                OffsetDateTime.now(),
                toDoData.dateTime?.minusHours(2)
            )
            val data = Data.Builder()
            val stringPriority = parsePriorityToString(view, toDoData.priority)
            val stringDeadLine =
                " ${view.context.getString(R.string.deadline)}: " +
                        "${deadLine.getDate()} " +
                        "${deadLine.getTime()}"

            data.putString(ToDoConstants.EXTRA_TASK_NAME, toDoData.title)
            data.putString(ToDoConstants.EXTRA_TASK_PRIORITY, stringPriority)
            data.putString(ToDoConstants.EXTRA_TASK_DEADLINE, stringDeadLine)
            data.putInt(ToDoConstants.EXTRA_TASK_ID, toDoData.id)

            val workRequest = OneTimeWorkRequest.Builder(NotificationWorkManager::class.java)
                .setInitialDelay(timeTilFuture, TimeUnit.MILLISECONDS)
                .setInputData(data.build())
                .addTag(toDoData.id.toString() + toDoData.title)
                .build()
            workManager.enqueue(workRequest)
        }
    }

    private fun parsePriorityToString(view: View, priority: Priority): String {
        val arrayStringPriorities = view.context.resources.getStringArray(R.array.priorities)
        return when (priority) {
            Priority.HIGH -> arrayStringPriorities[0]
            Priority.MEDIUM -> arrayStringPriorities[1]
            Priority.LOW -> arrayStringPriorities[2]
        }
    }

}
