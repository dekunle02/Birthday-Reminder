package com.adeleke.samad.birthdayreminder.ui.birthdayDetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adeleke.samad.birthdayreminder.BIRTHDAY_PICTURES
import com.adeleke.samad.birthdayreminder.BIRTHDAY_REFERENCE
import com.adeleke.samad.birthdayreminder.UiState
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class BirthdayDetailViewModel(var birthdayId: String) : ViewModel() {
    private val currentUserId = Firebase.auth.currentUser?.uid
    private val databaseRef =
        currentUserId?.let {
            birthdayId?.let { it1 ->
                Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(it).child(
                    it1
                )
            }
        }

    // LiveData
    private val _snack = MutableLiveData<String?>()
    val snack: LiveData<String?>
        get() = _snack

    private val _uiState = MutableLiveData<UiState?>()
    val uiState: LiveData<UiState?>
        get() = _uiState

    private val _birthday = MutableLiveData<Birthday?>()
    val birthday: LiveData<Birthday?>
        get() = _birthday


    private val _canCloseActivity = MutableLiveData<Boolean?>()
    val canCloseActivity: LiveData<Boolean?>
        get() = _canCloseActivity


    init {
        loadBirthdayInfo()
    }

    private fun loadBirthdayInfo() {
        _uiState.value = UiState.LOADING

        databaseRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val birthday = snapshot.getValue<Birthday>()
                _birthday.value = birthday
                _uiState.value = UiState.ACTIVE
            }

            override fun onCancelled(error: DatabaseError) {
                _snack.value = "Error retrieving birthday information.."
                Log.d(TAG, "errorFetching birthdayInfo: ${error.message}")
                _uiState.value = UiState.ERROR
            }
        })

    }

    fun favorite() {
        databaseRef?.child("favorite")?.setValue(true)?.addOnSuccessListener {
            _snack.value = "Added to favorites"
        }
            ?.addOnFailureListener {
                _snack.value = "Error. Unable to add to favorites.."
                Log.d(TAG, "favorite: Error on adding ${it.message}")
            }
    }

    fun unFavorite() {
        databaseRef?.child("favorite")?.setValue(false)?.addOnSuccessListener {
            _snack.value = "Removed from favorites"
        }
            ?.addOnFailureListener {
                _snack.value = "Error. Unable to remove from favorites.."
                Log.d(TAG, "favorite: Error on removing ${it.message}")
            }
    }

    fun archive() {
        databaseRef?.child("archived")?.setValue(true)?.addOnSuccessListener {
            _snack.value = "Birthday Archived!"
            _birthday.value?.archived = true
        }
            ?.addOnFailureListener {
                _snack.value = "Error. Unable to archive birthday.."
                Log.d(TAG, "archive: Error on archiving ${it.message}")
            }
    }

    fun restore() {
        databaseRef?.child("archived")?.setValue(false)?.addOnSuccessListener {
            _snack.value = "Birthday Restored!"
            _birthday.value?.archived = false
        }
            ?.addOnFailureListener {
                _snack.value = "Error. Unable to restore birthday.."
                Log.d(TAG, "archive: Error on restoring ${it.message}")
            }
    }

    fun delete() {
        _uiState.value = UiState.LOADING
        databaseRef?.removeValue()?.addOnSuccessListener {
            _snack.value = "Birthday has been permanently deleted"
            _canCloseActivity.value = true;

            val storageRef: StorageReference = FirebaseStorage.getInstance().getReference(
                BIRTHDAY_PICTURES
            ).child(currentUserId!!).child("$birthdayId.jpg")
            storageRef.delete().addOnSuccessListener {
            }
        }
    }

    companion object {
        const val TAG = "BirthdayDetailViewModel"
    }

    // Factory class to pass id into viewmodel object
    class BirthdayDetailViewModelFactory(
        private val birthdayId: String
    ) : ViewModelProvider.Factory {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BirthdayDetailViewModel::class.java)) {
                return BirthdayDetailViewModel(birthdayId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}