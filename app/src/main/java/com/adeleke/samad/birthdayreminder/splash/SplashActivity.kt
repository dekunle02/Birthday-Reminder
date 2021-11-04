package com.adeleke.samad.birthdayreminder.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.adeleke.samad.birthdayreminder.MainActivity
import com.adeleke.samad.birthdayreminder.R
import com.adeleke.samad.birthdayreminder.ui.auth.VerifyUserActivity
import com.adeleke.samad.birthdayreminder.ui.auth.signin.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase Auth
        auth = Firebase.auth

        Firebase.database.setPersistenceEnabled(true)

    }

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            continueToMainApp()
        } else {
            continueToAuthFlow()
        }
    }

    private fun continueToMainApp() {
        startActivity(Intent(this, VerifyUserActivity::class.java))
        finish()
    }

    private fun continueToAuthFlow() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

}