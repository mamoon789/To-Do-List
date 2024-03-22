package com.example.todolist.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todolist.R
import com.example.todolist.worker.ToDoWorker
import com.example.todolist.viewmodel.ToDoViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity()
{
    val vm: ToDoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
            Using Worker to send daily Tasks notifications.
            Requesting permission for Android Oreo and above.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            requestPermission()
        } else
        {
            initWorker()
        }
    }

    private fun initWorker()
    {
        val workRequest = PeriodicWorkRequestBuilder<ToDoWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setInitialDelay(
            duration = 5,
            timeUnit = TimeUnit.SECONDS
        ).setBackoffCriteria(
            backoffPolicy = BackoffPolicy.LINEAR,
            backoffDelay = 15,
            timeUnit = TimeUnit.SECONDS
        ).build()
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "task_notification_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission()
    {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
        {
            initWorker()
        } else
        {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty())
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                initWorker()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
            {
                showSnackBar(false)
            } else
            {
                showSnackBar(true)
            }
        }
    }

    private fun showSnackBar(
        action: Boolean
    )
    {
        val msg = "Permission required to send daily tasks alerts"
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).apply {
            if (action)
            {
                setAction("Settings") {
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${packageName}")
                    ).apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                }
            }
            show()
        }
    }
}