package com.adeleke.samad.birthdayreminder

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.adeleke.samad.birthdayreminder.services.ReminderWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.homeFragment
        bottomNav.setupWithNavController(
            findNavController(R.id.nav_host_fragment)
        )


        //                WORK MANAGER FOR NOTIFICATION REMINDERS
        val reminderWorkRequest : PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "makeReminders",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWorkRequest
            )

    }

    companion object{
        val TAG = "MainActivity"
    }
}