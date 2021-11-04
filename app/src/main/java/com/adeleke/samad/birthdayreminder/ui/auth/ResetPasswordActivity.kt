package com.adeleke.samad.birthdayreminder.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.adeleke.samad.birthdayreminder.R
import com.adeleke.samad.birthdayreminder.databinding.ActivityResetPasswordBinding
import com.adeleke.samad.birthdayreminder.makeSimpleSnack
import com.adeleke.samad.birthdayreminder.validEmail
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //      ViewBinding
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


//        Click listeners
        binding.buttonResetPassword.setOnClickListener {
            toggleStateToLoading()
            if (binding.editTextPasswordReset.validEmail() && binding.editTextPassword2Reset.validEmail()){
                if (binding.editTextPassword2Reset.text.toString().equals(binding.editTextPasswordReset.text.toString())) {
                    setNewPassword(binding.editTextPasswordReset.text.toString())
                }
            }
        }

        binding.buttonCancelReset.setOnClickListener {
            finish()
        }
    }

    private fun setNewPassword(password: String) {
        val user = Firebase.auth.currentUser
        user!!.updatePassword(password)
            .addOnCompleteListener { task ->
                toggleStateToActive()
                if (task.isSuccessful) {
                    binding.buttonResetPassword.makeSimpleSnack(getString(R.string.password_updated))
                } else {
                    binding.buttonResetPassword.makeSimpleSnack(getString(R.string.password_updated_failed))
                }
            }
    }

    private fun toggleStateToLoading() {
        binding.lottieEye.repeatMode = LottieDrawable.RESTART
        binding.lottieEye.repeatCount = 10
        binding.editTextPassword2Reset.isFocusable = false
        binding.editTextPasswordReset.isFocusable = false
        binding.buttonResetPassword.isFocusable = false
    }

    private fun toggleStateToActive() {
        binding.lottieEye.repeatCount = 0
        binding.editTextPasswordReset.isFocusable = true
        binding.editTextPassword2Reset.isFocusable = true
        binding.buttonResetPassword.isFocusable = true

    }

}