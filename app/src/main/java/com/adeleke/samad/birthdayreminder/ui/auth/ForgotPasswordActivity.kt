package com.adeleke.samad.birthdayreminder.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.adeleke.samad.birthdayreminder.R
import com.adeleke.samad.birthdayreminder.databinding.ActivityForgotPasswordBinding
import com.adeleke.samad.birthdayreminder.hideSoftKeyboard
import com.adeleke.samad.birthdayreminder.makeSimpleSnack
import com.adeleke.samad.birthdayreminder.validEmail
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //      ViewBinding
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        Set up the toolbar up button
        setSupportActionBar(binding.toolbarForgotPassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        Click listeners
        binding.buttonForgotPassword.setOnClickListener{
            it.hideSoftKeyboard()
            if (binding.editTextEmailForgot.validEmail()) {
                toggleStateToLoading()
                sendForgotPasswordEmail(binding.editTextEmailForgot.text.toString())
                binding.buttonForgotPassword.text = getString(R.string.resend)
            }
        }
    }

    private fun sendForgotPasswordEmail(email: String) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                toggleStateToActive()
                if (task.isSuccessful) {
                    binding.buttonForgotPassword.makeSimpleSnack(getString(R.string.email_sent))
                } else {
                    binding.buttonForgotPassword.makeSimpleSnack("Email not sent. Error-${task.exception?.message}")
                }
            }
    }

    private fun toggleStateToLoading() {
        binding.lottiePadlock.repeatMode = LottieDrawable.RESTART
        binding.lottiePadlock.repeatCount = 10
        binding.editTextEmailForgot.isFocusable = false
        binding.buttonForgotPassword.isFocusable = false
    }

    private fun toggleStateToActive() {
        binding.lottiePadlock.repeatCount = 0
        binding.editTextEmailForgot.isFocusable = true
        binding.buttonForgotPassword.isFocusable = true

    }


}