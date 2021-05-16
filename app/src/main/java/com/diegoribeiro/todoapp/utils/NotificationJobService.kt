package com.diegoribeiro.todoapp.utils

import android.app.job.JobParameters
import android.app.job.JobService

class NotificationJobService : JobService(){
    override fun onStartJob(params: JobParameters?): Boolean {
        //val todoTitle = params.extras.getString()
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    companion object{
        private val TAG: String = NotificationJobService::javaClass.name
    }
}