package com.adeleke.samad.birthdayreminder.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.adeleke.samad.birthdayreminder.MainActivity
import com.adeleke.samad.birthdayreminder.R
import com.adeleke.samad.birthdayreminder.databinding.ActivityVerifyUserBinding
import com.adeleke.samad.birthdayreminder.makeSimpleSnack
import com.adeleke.samad.birthdayreminder.ui.auth.signin.SignInActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class VerifyUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyUserBinding

    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //      ViewBinding
        binding = ActivityVerifyUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        USER init
        if (Firebase.auth.currentUser == null) {
            val i = Intent(this, SignInActivity::class.java)
            startActivity(i)
            finish()
        }
        user = Firebase.auth.currentUser!!
        checkVerificationAndSendEmail()


//        CLick listeners
        binding.buttonAuthenticate.setOnClickListener {sendVerificationEmail()}

    }

    override fun onResume() {
        super.onResume()
        checkVerification()
    }


    private fun checkVerificationAndSendEmail() {
        if (user.isEmailVerified) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
            sendVerificationEmail()
        }
    }

    private fun checkVerification() {
        if (user.isEmailVerified) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun sendVerificationEmail() {

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.buttonAuthenticate.makeSimpleSnack(getString(R.string.verification_sent))
                } else {
                    binding.buttonAuthenticate.makeSimpleSnack(getString(R.string.verification_error))
                }
            }
    }



}