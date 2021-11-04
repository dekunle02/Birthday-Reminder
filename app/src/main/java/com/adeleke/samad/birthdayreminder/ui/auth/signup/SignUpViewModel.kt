package com.adeleke.samad.birthdayreminder.ui.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adeleke.samad.birthdayreminder.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class SignUpViewModel : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth

    // LiveData
    private val _snack = MutableLiveData<String?>()
    val snack: LiveData<String?>
        get() = _snack

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState?>
        get() = _uiState

    private val _canNavigateToApp: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val canNavigateToApp: LiveData<Boolean>
        get() = _canNavigateToApp


    fun signUpWithEmailAndPassword(name: String, email: String, password: String) {
        _uiState.value = UiState.LOADING
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _uiState.value = UiState.ACTIVE
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener{ p ->
                            if (p.isSuccessful) {
                                _snack.value = "Account Created Successfully"
                                _canNavigateToApp.value = true
                            } else {
                                _snack.value = "Account creation failed. Error-${task.exception?.message}"
                            }
                        }

                } else {
                    _snack.value = "Authentication failed. Error-${task.exception?.message}"
                    _canNavigateToApp.value = false
                }
            }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        _uiState.value = UiState.LOADING
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _uiState.value = UiState.ACTIVE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    _snack.value = "Sign in Successful"
                    _canNavigateToApp.value = true
                } else {
                    // If sign in fails, display a message to the user.
                    _snack.value = "Authentication failed. Error:${task.exception}"
                    _canNavigateToApp.value = false
                }
            }
    }

}