package com.diegoribeiro.todoapp.utils

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.diegoribeiro.todoapp.fragments.add.AddFragment
import java.lang.IllegalArgumentException

class NotificationJobService : JobService(){
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        val toDoName = jobParameters.extras.getString(AddFragment::javaClass.name)
        Log.d(JOB_SCHEDULER_SERVICE, "JOB_SCHEDULER_SERVICE" ?: throw  IllegalArgumentException("No name"))
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    companion object{
        private val TAG: String = NotificationJobService::javaClass.name
    }
}