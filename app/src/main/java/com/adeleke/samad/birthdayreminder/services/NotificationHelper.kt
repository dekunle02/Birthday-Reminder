package com.adeleke.samad.birthdayreminder.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.model.Birthday
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NotificationHelper {

    companion object {
        const val TAG = "NotificationHelper"

        private fun createNotificationChannel(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    PRIMARY_NOTIFICATION_CHANNEL_ID,
                    "BirthdayReminder Notification", NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.enableVibration(true)
                notificationChannel.description =
                    context.getString(R.string.notification_channel_description)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        fun postBirthdayNotification(context: Context, birthday: Birthday) {

            Log.d(TAG, "postBirthdayNotification: called with $birthday")

            createNotificationChannel(context)
            val notification = birthday.buildNotification(context)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(generateRandomNumber(),notification)
        }

        private fun Birthday.buildNotification(context: Context): Notification {
            val smsIntent = this.makeSMSIntent()
            var smsPendingIntent: PendingIntent? = null
            if (smsIntent.resolveActivity(context.packageManager) != null) {
                smsPendingIntent = PendingIntent.getActivity(
                    context,
                    NOTIFICATION_BROADCAST_REQUEST_CODE,
                    smsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            Log.d(TAG,"buildNotification: called. is the birthday Today ${this.willBeToday()}, is the birthday tomorrow ${this.willBeTomorrow()}")

            var titleMessage = "It's ${this.name}'s birthday today!"
            if (this.willBeTomorrow()) titleMessage = "${this.name}'s birthday is tomorrow"

            Log.d(TAG, "buildNotification: Title message is $titleMessage")

            val notificationBuilder =  NotificationCompat.Builder(context, PRIMARY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(titleMessage)
                .setContentText(context.getString(R.string.send))
                .setSmallIcon(R.drawable.ic_birthday)
                .setContentIntent(smsPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            return notificationBuilder.build()
        }


        fun postMonthNotification(context: Context, month: Int) {
            Log.d("MainActivity", "postBirthMonthNotification: called")

            createNotificationChannel(context)
            GlobalScope.launch{
                val allBirthdays = FirebaseService.fetchAllBirthdays()
                val monthBirthdays = allBirthdays.filter { it?.monthOfBirth == month }
                val notification = monthBirthdays.buildMonthNotification(context)
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(generateRandomNumber(),notification)
            }
        }
        private fun List<Birthday?>.buildMonthNotification(context: Context): Notification {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_BROADCAST_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val monthText = if(this.isNullOrEmpty()) "No Birthdays this month" else "${this.size} this month!"
            val notificationBuilder = NotificationCompat.Builder(context, PRIMARY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Birthdays this month!")
                .setContentText(monthText)
                .setSmallIcon(R.drawable.ic_birthday)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
            return notificationBuilder.build()
        }



    }


}