package com.adeleke.samad.birthdayreminder.ui.birthdayList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adeleke.samad.birthdayreminder.BIRTHDAY_REFERENCE
import com.adeleke.samad.birthdayreminder.UiState
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.adeleke.samad.birthdayreminder.sortByDate
import com.adeleke.samad.birthdayreminder.sortByName
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class BirthdayListViewModel : ViewModel() {

    enum class ListState {
        DEFAULT, FAVORITES, ARCHIVED, SORT_NAME, SORT_DATE
    }

    // LiveData
    private val _snack = MutableLiveData<String?>()
    val snack: LiveData<String?>
        get() = _snack

    private val _uiState = MutableLiveData<UiState?>()
    val uiState: LiveData<UiState?>
        get() = _uiState

    private val _recyclerData = MutableLiveData<MutableList<Birthday?>>()
    val recyclerData: LiveData<MutableList<Birthday?>>
        get() = _recyclerData

    // Private variables
    private var state: ListState = ListState.DEFAULT
    private var previousState: ListState = ListState.DEFAULT

    init {
        makeDefaultList()
    }

    // List preparation functions
    private fun makeDefaultList() {
        _uiState.value = UiState.LOADING
        val currentUserId = Firebase.auth.currentUser?.uid
        val databaseRef =
            currentUserId?.let { Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(it) }

        databaseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _uiState.value = UiState.ACTIVE

                val birthdayList = mutableListOf<Birthday?>()
                for (singleSnapshot in snapshot.children) {
                    val birthday = singleSnapshot.getValue<Birthday>()
                    birthday?.let { birthdayList.add(it) }
                }
                val defaultList = birthdayList.filter { it?.archived == false }.sortByDate()
                _recyclerData.value = defaultList
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = UiState.ERROR
                _snack.value = "Error getting birthdays. Error:${error.message}"
            }
        })
    }

    private fun makeArchiveList() {
        _uiState.value = UiState.LOADING
        val currentUserId = Firebase.auth.currentUser?.uid
        val databaseRef =
            currentUserId?.let { Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(it) }

        databaseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _uiState.value = UiState.ACTIVE

                val birthdayList = mutableListOf<Birthday?>()
                for (singleSnapshot in snapshot.children) {
                    val birthday = singleSnapshot.getValue<Birthday>()
                    birthday?.let { birthdayList.add(it) }
                }
                val archiveList = (birthdayList.filter { it?.archived == true }).sortByDate()
                _recyclerData.value = archiveList

            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = UiState.ERROR
                _snack.value = "Error getting birthdays. Error:${error.message}"
            }
        })
    }

    private fun makeFavoriteList() {
        _uiState.value = UiState.LOADING
        val currentUserId = Firebase.auth.currentUser?.uid
        val databaseRef =
            currentUserId?.let { Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(it) }

        databaseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _uiState.value = UiState.ACTIVE

                val birthdayList = mutableListOf<Birthday?>()
                for (singleSnapshot in snapshot.children) {
                    val birthday = singleSnapshot.getValue<Birthday>()
                    birthday?.let { birthdayList.add(it) }
                }
                val favoriteList = (birthdayList.filter { it?.favorite == true }).sortByDate()
                _recyclerData.value = favoriteList

            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = UiState.ERROR
                _snack.value = "Error getting birthdays. Error:${error.message}"
            }
        })
    }

    private fun sortByName() {
        _uiState.value = UiState.LOADING
        _recyclerData.value = _recyclerData.value?.sortByName()
        _uiState.value = UiState.ACTIVE
    }

    private fun sortByDate() {
        _uiState.value = UiState.LOADING
        _recyclerData.value = _recyclerData.value?.sortByDate()
        _uiState.value = UiState.ACTIVE
    }


    // Exposed methods

    fun changeState(newState: ListState) {
        _uiState.value = UiState.LOADING

        previousState = this.state
        when (newState) {
            ListState.DEFAULT -> makeDefaultList()
            ListState.ARCHIVED -> makeArchiveList()
            ListState.FAVORITES -> makeFavoriteList()
            ListState.SORT_NAME -> sortByName()
            ListState.SORT_DATE -> sortByDate()
        }
        state = newState
    }

    fun refreshState() {
        val ps = previousState
        when (val ns = state) {
            ListState.DEFAULT -> makeDefaultList()
            ListState.ARCHIVED -> makeArchiveList()
            ListState.FAVORITES -> makeFavoriteList()
            ListState.SORT_NAME -> {
                changeState (ps)
                changeState (ns)
            }
            ListState.SORT_DATE -> {
                changeState(ps)
                changeState(ns)
            }
        }
    }


    companion object {
        const val TAG = "BirthdayListViewModel"
    }
}