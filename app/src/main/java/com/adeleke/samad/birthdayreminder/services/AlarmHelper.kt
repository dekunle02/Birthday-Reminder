package com.adeleke.samad.birthdayreminder.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.model.Birthday
import java.time.LocalDate
import java.util.*

class AlarmHelper {

    companion object {
        const val TAG = "AlarmHelper"

        fun scheduleBirthdayMorningOfDayReminder(context: Context, birthday: Birthday) {
            Log.d(TAG, "scheduleBirthdayMorningOfDayReminder: Called with birthday $birthday")

            val cal = Calendar.getInstance()
            cal.set(Calendar.AM_PM, 0)
            cal.set(Calendar.HOUR, 7)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DATE, 1)

            Log.d(
                TAG,
                "scheduleBirthdayMorningOfDayReminder: Called with calendar $cal"
            )

            val triggerTime = cal.timeInMillis
            val birthdayJson = birthday.toJSON()
            val notificationIntent: Intent = Intent(context, NotificationReceiver::class.java)
            notificationIntent.action = BIRTHDAY_NOTIFICATION_ACTION
            notificationIntent.putExtra(BIRTHDAY_INTENT_TAG, birthdayJson)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRandomNumber(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        fun scheduleBirthdayNextDayReminder(context: Context, birthday: Birthday) {
            Log.d(TAG, "scheduleBirthdayNextDayReminder: called with birthday $birthday")

            val cal = Calendar.getInstance()
            cal.set(Calendar.AM_PM, 1)
            cal.set(Calendar.HOUR, 8)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.MILLISECOND, 0)

            Log.d(TAG, "scheduleBirthdayNextDayReminder: called with calender -> $cal.")

            val triggerTime = cal.timeInMillis
            val birthdayJson = birthday.toJSON()

            val notificationIntent: Intent = Intent(context, NotificationReceiver::class.java)
            notificationIntent.action = BIRTHDAY_NOTIFICATION_ACTION
            notificationIntent.putExtra(BIRTHDAY_INTENT_TAG, birthdayJson)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRandomNumber(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        fun scheduleMonthReminder(context: Context, month: Int) {
            Log.d(TAG, "scheduleMonthReminder: called with birthday $month")

            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, 1)
            cal.set(Calendar.AM_PM, 0)
            cal.set(Calendar.HOUR, 8)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.MILLISECOND, 0)

            Log.d(TAG, "scheduleMonthReminder: called with calendar -> $cal")

            val triggerTime = cal.timeInMillis

            val notificationIntent: Intent = Intent(context, NotificationReceiver::class.java)
            notificationIntent.action = MONTH_NOTIFICATION_ACTION
            notificationIntent.putExtra(MONTH_VALUE_INTENT_TAG, month)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRandomNumber(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }


        fun scheduleTestReminder(context: Context, birthday: Birthday) {
            Log.d(TAG, "scheduleTestReminder: Called with $birthday")
            val triggerTime: Long = 1000
            val birthdayJson = birthday.toJSON()

            val notificationIntent: Intent = Intent(context, NotificationReceiver::class.java)
            notificationIntent.action = BIRTHDAY_NOTIFICATION_ACTION
            notificationIntent.putExtra(BIRTHDAY_INTENT_TAG, birthdayJson)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRandomNumber(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

}