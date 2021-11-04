package com.adeleke.samad.birthdayreminder.model

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.adeleke.samad.birthdayreminder.monthColorMap
import com.adeleke.samad.birthdayreminder.toFormattedString
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.Period
import java.util.*

@Parcelize
data class Birthday(
    var id: String? = generateID(),
    var userId: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var dayOfBirth: Int = today.dayOfMonth,
    var monthOfBirth: Int = today.monthValue,
    var yearOfBirth: Int = today.year,
    var phoneNumber: String? = null,
    var email: String? = null,
    var message: String? = null,
    var notes: String? = null,
    var favorite: Boolean? = false,
    var archived: Boolean? = false
) : Parcelable {

    companion object {
        private const val TAG = "BirthdayModel"
        private const val prefix = "birthday-"
        private fun generateID(): String = prefix + UUID.randomUUID().toString()

        fun fromJson(json: String): Birthday {
            val gson = Gson()
            return gson.fromJson(json, Birthday::class.java)
        }

        private val today = LocalDate.now()
    }

    override fun toString(): String {
        return this.name.toString() + "-" + this.date()
            .toString() + "-" + "id= ${this.id}, date= ${this.date()}, name=${this.name}"
    }

    //    Display functions
    fun date(): LocalDate {
        var date = LocalDate.now()
        date = date.withDayOfMonth(this.dayOfBirth).withMonth(this.monthOfBirth)
        yearOfBirth?.let { year -> date = date.withYear(year) }
        return date
    }

    fun monthColor(): Int? {
        return monthColorMap[this.monthOfBirth]
    }

    fun shareMessage(): String {
        return this.name + "'s next birthday is on " + this.nextBirthday()
            .toFormattedString() + ". They are ${this.ageString()}old."
    }

    private fun currentAge(): Period? {
        val today = LocalDate.now()

        return if (this.yearOfBirth == null) {
            null
        } else {
            val birthDate = this.yearOfBirth?.let {
                today.withDayOfMonth(this.dayOfBirth).withMonth(this.monthOfBirth).withYear(it)
            }
            Period.between(birthDate, today)
        }
    }

    fun ageString(): String? {
        val period = this.currentAge()
        period?.let { it ->
            val _y: String = if (it.years == 1) " year " else " years "
            val _m: String = if (it.months == 1) " month " else " months "
            val _d: String = if (it.days == 1) " day " else " days "
            return "${it.years}$_y${it.months}$_m${it.days}$_d"
        } ?: return null
    }

    fun nextBirthday(): LocalDate {
        val today = LocalDate.now()
        val birthdayThisYear = today.withDayOfMonth(this.dayOfBirth).withMonth(this.monthOfBirth)
        return if (today.isAfter(birthdayThisYear)) {
            birthdayThisYear.withYear(today.year + 1)
        } else {
            birthdayThisYear
        }
    }


    //    Filtering Functions
    fun willBeTomorrow(): Boolean {
        val today = LocalDate.now()
        val birthdayThisYear = today.withDayOfMonth(this.dayOfBirth).withMonth(this.monthOfBirth)
        return (birthdayThisYear.isEqual(today.plusDays(1)))
    }

    fun willBeThisMonth(): Boolean {
        val today = LocalDate.now()
        return (this.monthOfBirth == today.monthValue)
    }

    fun willBeToday(): Boolean {
        val today = LocalDate.now()
        val birthdayThisYear = today.withDayOfMonth(this.dayOfBirth).withMonth(this.monthOfBirth)
        return (birthdayThisYear.isEqual(today))
    }


    //    Other functions
// Intent that opens the Message App with the birthday data
    fun makeSMSIntent(): Intent {
        val smsNumber = "smsto:" + this.phoneNumber
        val smsMessage = this.message
        val smsIntent = Intent(Intent.ACTION_SENDTO)
        smsIntent.data = Uri.parse(smsNumber)
        smsIntent.putExtra("sms_body", smsMessage)
        return smsIntent
    }

    fun toJSON(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

}
