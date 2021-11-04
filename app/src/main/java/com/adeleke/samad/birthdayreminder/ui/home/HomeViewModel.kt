package com.adeleke.samad.birthdayreminder.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adeleke.samad.birthdayreminder.UiState
import com.adeleke.samad.birthdayreminder.filterFromDate
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.adeleke.samad.birthdayreminder.services.FirebaseService
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel : ViewModel() {

    //  Coroutines
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // LiveData
    private val _snack = MutableLiveData<String?>()
    val snack: LiveData<String?>
        get() = _snack

    private val _uiState = MutableLiveData<UiState?>()
    val uiState: LiveData<UiState?>
        get() = _uiState

    private val _recentBirthdays = MutableLiveData<List<Birthday?>>()
    val recentBirthdays: LiveData<List<Birthday?>>
        get() = _recentBirthdays

    private val _statMap = MutableLiveData<Map<String, Int>?>()
    val statMap: LiveData<Map<String, Int>?>
        get() = _statMap

    private val _calendarDates = MutableLiveData<List<CalendarDay>>()
    val calendarDates: LiveData<List<CalendarDay>>
        get() = _calendarDates

    private val _monthBirthdays = MutableLiveData<List<Birthday?>>()
    val monthBirthdays: LiveData<List<Birthday?>>
        get() = _monthBirthdays


    private lateinit var allBirthdays: List<Birthday?>

    init {
        // Essentially sets up the homepage
        uiScope.launch {
            _uiState.value = UiState.LOADING

            allBirthdays = FirebaseService.fetchAllBirthdays()
            if (allBirthdays.isNullOrEmpty()) {
                _uiState.value = UiState.EMPTY
            } else {
                _uiState.value = UiState.ACTIVE
            }
            makeUpcomingList()
            makeStatMap()
            fetchBirthdaysForCalendar(CalendarDay.today())
        }

    }

    fun fetchBirthdaysForCalendar(currentDate: CalendarDay) {
        val birthdaysThatMonth = allBirthdays.filter {it?.monthOfBirth == currentDate.month}
        birthdaysThatMonth.also { _monthBirthdays.value = it }

        val datesThatMonth = mutableListOf<CalendarDay>()
        birthdaysThatMonth.forEach {
            datesThatMonth.add(CalendarDay.from(currentDate.year, currentDate.month, it!!.dayOfBirth))
        }
        _calendarDates.value = datesThatMonth.toList()
    }


    private fun makeUpcomingList() {
        val upcomingList = allBirthdays.filterFromDate(LocalDate.now())
        _recentBirthdays.value = upcomingList
    }

    private fun makeStatMap() {
        val birthdaysToday = allBirthdays.filter { it?.willBeToday() == true }
        val todayCount = birthdaysToday.size

        val birthdaysThisMonth = allBirthdays.filter { it?.willBeThisMonth() == true }
        val monthCount = birthdaysThisMonth.size

        val yearCount = allBirthdays.size

        _statMap.value = mapOf<String, Int>(
            "today" to todayCount,
            "month" to monthCount,
            "year" to yearCount
        )
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val TAG = "HomeViewModel"
    }
}