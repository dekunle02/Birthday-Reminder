package com.adeleke.samad.birthdayreminder.ui.birthdayEdit

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Operation
import com.adeleke.samad.birthdayreminder.BIRTHDAY_PICTURES
import com.adeleke.samad.birthdayreminder.BIRTHDAY_REFERENCE
import com.adeleke.samad.birthdayreminder.ServiceResponse
import com.adeleke.samad.birthdayreminder.UiState
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.adeleke.samad.birthdayreminder.services.FirebaseService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.log

class BirthdayEditViewModel : ViewModel() {

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

    private val _canNavigateBack = MutableLiveData<Boolean?>()
    val canNavigateBack: LiveData<Boolean?>
        get() = _canNavigateBack

    private val _birthday = MutableLiveData<Birthday?>()
    val birthday: LiveData<Birthday?>
        get() = _birthday


    private var currentImageBitmap: Bitmap? = null
    private var hasChangedImage: Boolean = false


    fun retrieveBirthdayWithId(birthdayId: String) {
        _uiState.value = UiState.LOADING

        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = UiState.ERROR
            return
        }

        val databaseRef = Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(currentUserId)
            .child(birthdayId)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val birthday = snapshot.getValue<Birthday>()
                _birthday.value = birthday
                _uiState.value = UiState.ACTIVE
            }

            override fun onCancelled(error: DatabaseError) {
                _snack.value = "Error retrieving birthday"
                _uiState.value = UiState.ERROR
            }
        })

    }

    fun makeNewBirthday() {
        val newBirthday = Birthday()
        _birthday.value = newBirthday
    }

    fun saveBirthday(birthday: Birthday) {
        _uiState.value = UiState.LOADING
        val currentBirthdayId = _birthday.value?.id
        val currentBirthdayUserId = _birthday.value?.userId
        birthday.id = currentBirthdayId
        birthday.userId = currentBirthdayUserId
        uiScope.launch {
            if(!hasChangedImage) {
                birthday.imageUrl = _birthday.value?.imageUrl
            } else {
                val uploadResponse = FirebaseService.uploadBirthdayPicture(currentImageBitmap, birthday)
                if (uploadResponse is ServiceResponse.Success) {
                    val url = uploadResponse.value
                    birthday.imageUrl = url
                }
            }
            val birthdayUpdateResponse = FirebaseService.saveOrUpdateBirthday(birthday)
           if (birthdayUpdateResponse is ServiceResponse.Success) {
               _snack.value = birthdayUpdateResponse.value
           } else {
               _snack.value = "Error saving the birthday"
           }
        }
        _uiState.value = UiState.ACTIVE
        _canNavigateBack.value = true
    }

    fun changeCurrentBitmap(bitmap: Bitmap) {
        this.hasChangedImage = true
        this.currentImageBitmap = bitmap
    }

    fun deleteBirthday() {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = UiState.ERROR
            return
        }
        val birthdayId = _birthday.value?.id
        if (birthdayId == null) {
            _uiState.value = UiState.ERROR
            return
        }
        val databaseRef =
            Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(currentUserId)
                .child(birthdayId)
        databaseRef.removeValue().addOnSuccessListener {
            _snack.value = "Birthday has been permanently deleted"

            val storageRef: StorageReference = FirebaseStorage.getInstance().getReference(
                BIRTHDAY_PICTURES
            ).child(currentUserId!!).child("$birthdayId.jpg")
            storageRef.delete().addOnSuccessListener {
                _uiState.value = UiState.ACTIVE
                _canNavigateBack.value = true
            }
        }

    }

    fun discardChanges() {
        _uiState.value = UiState.LOADING
        _canNavigateBack.value = true
    }


//  Clearing the coroutine job
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val TAG = "BirthdayEditViewModel"
    }
}