package com.diegoribeiro.todoapp.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.ListFragment
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.diegoribeiro.todoapp.MainActivity
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.ToDoConstants
import com.diegoribeiro.todoapp.fragments.add.AddFragment

@RequiresApi(Build.VERSION_CODES.O)
class NotificationWorkManager(context: Context, parameters: WorkerParameters): Worker(context, parameters) {

    private val PRIMARY_CHANNEL_ID = "primary_channel_id"
    private val NOTIFICATION_ID = 0
    private var notificationWorkManager: NotificationManager? = null


    @SuppressLint("LongLogTag")
    override fun doWork(): Result {
        val name = inputData.getString(ToDoConstants.EXTRA_TASK_NAME)
        val priority = inputData.getString(ToDoConstants.EXTRA_TASK_PRIORITY)
        val deadLine = inputData.getString(ToDoConstants.EXTRA_TASK_DEADLINE)
        return if (name != null) {
            //Log.d("**Name", name)
            createChannel()
            sendNotification(name, "${priority.toString()} - ${deadLine.toString()}")
            Result.success()
        }else{
            return Result.failure()
        }
    }

    private fun sendNotification(title: String, contentText: String) {
        val notificationBuilder: NotificationCompat.Builder = getNotificationBuilder(title, contentText)

        //Delivery notification
        notificationWorkManager?.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getNotificationBuilder(title: String, contentText: String): NotificationCompat.Builder {

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        return NotificationCompat.Builder(applicationContext, PRIMARY_CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_time_notification)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

    }

    private fun createChannel() {
        //Create notification workManager
        notificationWorkManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            PRIMARY_CHANNEL_ID,
            applicationContext.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH)

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = applicationContext.getString(R.string.notification_channel_name)

        notificationWorkManager?.createNotificationChannel(notificationChannel)
    }
}