package com.adeleke.samad.birthdayreminder.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.model.Birthday
import java.time.LocalDate

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val intentAction = intent?.action
        Log.d(TAG, "onReceive: NOTIFICATION REQUEST RECEIVED!!")
        intentAction?.let { action ->
            when(action){
                BIRTHDAY_NOTIFICATION_ACTION -> {
                    val birthdayJSON = intent.getStringExtra(BIRTHDAY_INTENT_TAG)
                    birthdayJSON?.let { j ->
                        val birthday: Birthday = Birthday.fromJson(j)
                        if (context != null) {
                            NotificationHelper.postBirthdayNotification(context, birthday)
                        }
                    }
                }

                MONTH_NOTIFICATION_ACTION -> {
                    Log.d("MainActivity", "notification Receiver: called")

                    val monthIndex = LocalDate.now().monthValue
                    val month = intent.getIntExtra(MONTH_VALUE_INTENT_TAG, monthIndex)
                    if (context != null) {
                        NotificationHelper.postMonthNotification(context, month)
                    } else {return}
                }



                else -> {return}
            }
        }
    }

    companion object {
        const val TAG = "NotificationReceiver"
    }
}