package com.adeleke.samad.birthdayreminder.services

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.adeleke.samad.birthdayreminder.isLastDayOfMonth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.math.log

class ReminderWorker(private val appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork: CALLED")
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext)
        val showAllNotificationsPref: Boolean = sharedPref.getBoolean("allNotifications", true)
        val showMonthNotificationsPref: Boolean = sharedPref.getBoolean("monthlyNotifications", true)
        val showDayNotificationsPref: Boolean = sharedPref.getBoolean("dayNotifications", true)
        val showDayBeforeNotificationsPref: Boolean = sharedPref.getBoolean("dayBeforeNotifications", true)

        if (!showAllNotificationsPref){
            return Result.success()
        }

        GlobalScope.launch {
            val allBirthdays = FirebaseService.fetchAllBirthdays()
            val birthdaysTomorrow = allBirthdays.filter { it?.willBeTomorrow() == true }

            val sampleBirthday = allBirthdays[4];
            AlarmHelper.scheduleTestReminder(appContext, sampleBirthday!!)

            birthdaysTomorrow.forEach { it ->
                Log.d(TAG, "doWork: Each birthday tomorrow-> $it")
                it?.let { birthday ->
                    if (showDayNotificationsPref) {
                        AlarmHelper.scheduleBirthdayMorningOfDayReminder(appContext, birthday)
                    }
                    if (showDayBeforeNotificationsPref) {
                        AlarmHelper.scheduleBirthdayNextDayReminder(appContext, birthday)
                    }
                }
            }

            val today = LocalDate.now()
            if (today.isLastDayOfMonth() && showMonthNotificationsPref) {
                AlarmHelper.scheduleMonthReminder(appContext, today.monthValue + 1)
            }

        }

        return Result.success()
    }

    companion object{
        val TAG = "ReminderWorker"
    }
}