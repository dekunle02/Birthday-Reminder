package com.adeleke.samad.birthdayreminder

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

// Enum class for UI states
enum class UiState () {
    ACTIVE, LOADING, EMPTY, ERROR
}

sealed class ServiceResponse<out R> {
    class Success<R>(val value: R): ServiceResponse<R>()
    class Failure(val error: Throwable): ServiceResponse<Nothing>()
}


/*
* Constants
 */
const val BIRTHDAY_REFERENCE = "Birthdays"
const val BIRTHDAY_DETAIL_INTENT_KEY = "birthdayDetail"
const val BIRTHDAY_EDIT_INTENT_KEY = "birthdayEdit"
const val IMPORT_CONTACT_INTENT_KEY = "importContact"
const val BIRTHDAY_IS_NEW_KEY = "birthdayNew?"

const val CONTACT_REQUEST_CODE = 100
const val CONTACT_PERMISSION_REQUEST_CODE = 200
const val CAMERA_REQUEST_CODE = 101
const val GALLERY_REQUEST_CODE = 102

const val BIRTHDAY_PICTURES = "BirthdayPictures"
const val PROFILE_PICTURES = "ProfilePictures"

const val PRIMARY_NOTIFICATION_CHANNEL_ID = "birthdayReminder_notification_channel"
const val NOTIFICATION_BROADCAST_REQUEST_CODE = 200

const val BIRTHDAY_NOTIFICATION_ACTION = "birthdayAction"
const val MONTH_NOTIFICATION_ACTION = "monthAction"

const val BIRTHDAY_INTENT_TAG = "birthdayIntentTag"
const val MONTH_VALUE_INTENT_TAG = "monthIntentTag"

/*
* Extension Functions
**/
fun View. hideSoftKeyboard() {
    val imm =
        this.context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}
fun View.makeSimpleSnack(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}

// Functions to validate emails and password formatting
fun EditText.validEmail(): Boolean {
    val text = this.text.toString().trim()
    return if (text.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
        true
    } else {
        this.makeSimpleSnack(this.context.getString(R.string.enter_valid_email))
        false
    }
}
fun EditText.cleanName(): String? {
    val text = this.text.toString().trim()

    return if (text.isNotEmpty() && text.length >= 2) {
        this.setText(text.capitalize(Locale.getDefault()))
        this.text.toString()
    } else {
        this.makeSimpleSnack(this.context.getString(R.string.enter_valid_name))
        null
    }
}
fun EditText.cleanEmail(): String? {
    val text = this.text.toString().trim()
    return if (text.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
        this.text.toString()
    } else {
        this.makeSimpleSnack(this.context.getString(R.string.enter_valid_email))
        return null
    }
}

fun EditText.validatePasswordSuccessful(): Boolean {
    val text = this.text.toString().trim()
    return if (!text.isNullOrEmpty() && text.length >= 6) {
        true
    } else {
        this.makeSimpleSnack(context.getString(R.string.enter_valid_password))
        false
    }
}


// Function to format Local Date
fun LocalDate.toFormattedString() : String {
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(this)
}
fun String.toLocalDate() : LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
}


//    Functions to sort Birthday Lists
fun List<Birthday?>.sortByDate(): MutableList<Birthday?> {
    return this.sortedWith(compareBy<Birthday?> { it?.monthOfBirth }.thenBy { it?.dayOfBirth }).toMutableList()
}
fun List<Birthday?>.sortByName(): MutableList<Birthday?> {
    return this.sortedWith(compareBy<Birthday?> { it?.name }).toMutableList()
}
fun List<Birthday?>.filterFromDate(date: LocalDate): List<Birthday?>{

    val filteredList =  this.filter { it?.nextBirthday()?.isAfter(date.minusDays(1)) == true }
    val sortedList = filteredList.sortedBy { it?.nextBirthday() }
    return if (sortedList.size > 10) sortedList.take(10) else sortedList
}


fun generateRandomNumber(): Int {
    val random = Random()
    return random.nextInt(5000)
}

fun LocalDate.isLastDayOfMonth(): Boolean {
    val tomorrow = this.plusDays(1)
    return tomorrow.dayOfMonth == 1
}



val monthColorMap = mapOf(
    1 to R.color.card_1,
    2 to R.color.card_2,
    3 to R.color.card_3,
    4 to R.color.card_4,
    5 to R.color.card_5,
    6 to R.color.card_6,
    7 to R.color.card_7,
    8 to R.color.card_8,
    9 to R.color.card_9,
    10 to R.color.card_10,
    11 to R.color.card_11,
    12 to R.color.card_12,
)







