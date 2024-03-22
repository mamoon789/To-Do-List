package com.example.todolist.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.R
import com.example.todolist.repository.TODO_KEY
import com.example.todolist.receiver.ToDoBroadcastReceiver
import com.example.todolist.models.ToDo
import com.example.todolist.models.ToDoResponse
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

@HiltWorker
class ToDoWorker @AssistedInject constructor(
    private val dataStore: DataStore<Preferences>,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params)
{
    override suspend fun doWork(): Result
    {
        /*
            Creating notifications for daily tasks that are not completed.
         */

        return try
        {
            val date = SimpleDateFormat(
                "dd-MM-yyyy", Locale.getDefault()
            ).format(Calendar.getInstance().time)

            val key = stringPreferencesKey(TODO_KEY)
            val preferences = dataStore.data.first()
            val rawData = preferences[key]
            val data = Gson().fromJson(rawData, ToDoResponse::class.java)
            val todos = data.todos.filter { !it.completed && it.date == date }
            todos.forEach {
                createNotification(it)
            }

            Result.success()
        } catch (e: Exception)
        {
            Log.e("TAG", "doWork: worker failed, retrying in 15 seconds + ${e.message}")
            Result.retry()
        }
    }

    private fun createNotification(todo: ToDo)
    {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager

        val channelId = "channel_task_notification"
        val channelName = "Task Notifications"
        val actionName = "Mark Completed"
        val contentTitle = "Today's Task!"

        val markCompleteIntent = Intent(
            applicationContext, ToDoBroadcastReceiver::class.java
        ).apply {
            putExtra("id", todo.id)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE),
            markCompleteIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification).setContentTitle(contentTitle)
            .setContentText(todo.Title).setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_check, actionName, markCompletePendingIntent)
            .setAutoCancel(true).build()

        notificationManager.notify(Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE), notification)
    }
}