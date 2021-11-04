package com.adeleke.samad.birthdayreminder.ui.profile

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adeleke.samad.birthdayreminder.ServiceResponse
import com.adeleke.samad.birthdayreminder.model.ProfileInfo
import com.adeleke.samad.birthdayreminder.services.FirebaseService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    //  Coroutines
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // LiveData
    private val _snack = MutableLiveData<String?>()
    val snack: LiveData<String?>
        get() = _snack

    private val _profileInfo = MutableLiveData<ProfileInfo>()
    val profileInfo: LiveData<ProfileInfo>
        get() = _profileInfo


    init {
        getProfileInfo()
    }


    private fun getProfileInfo() {
        uiScope.launch {
            val user = Firebase.auth.currentUser

            var displayName = user?.displayName
            if (displayName.isNullOrEmpty()) {
                displayName = "Enter Name"
            }
            var displayPhotoUrl = ""
            val urlTask = FirebaseService.fetchProfilePhotoUrl()
            if (urlTask is ServiceResponse.Success) {
                displayPhotoUrl = urlTask.value
            }

            _profileInfo.value = ProfileInfo(userId = user!!.uid, displayName, displayPhotoUrl )

        }

    }


    fun uploadProfilePicture(bitmap: Bitmap?) {
        uiScope.launch {
            bitmap?.let {
                val response = FirebaseService.uploadProfilePhoto(it)
                if (response is ServiceResponse.Success) {
                    _snack.value = "Photo uploaded"
                } else if (response is ServiceResponse.Failure){
                    _snack.value = "Error uploading. " + response.error.message.toString()
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun setNewProfileName(newName: String) {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = newName
        }
        user!!.updateProfile(profileUpdates).addOnSuccessListener { _ ->
            val profileInfo: ProfileInfo? = _profileInfo.value
            profileInfo?.displayName = newName
            profileInfo?.let {
                it.displayName = newName
                _profileInfo.value = it
            }
        }
    }


    companion object {
        const val TAG = "ProfileViewModel"
    }
}