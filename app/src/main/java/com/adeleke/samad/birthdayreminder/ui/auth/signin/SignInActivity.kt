package com.adeleke.samad.birthdayreminder.ui.auth.signin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.ActivitySignInBinding
import com.adeleke.samad.birthdayreminder.ui.auth.ForgotPasswordActivity
import com.adeleke.samad.birthdayreminder.ui.auth.VerifyUserActivity
import com.adeleke.samad.birthdayreminder.ui.auth.signup.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class SignInActivity : AppCompatActivity() {

    private lateinit var viewModel: SignInViewModel
    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    // Configure Google Sign In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(OAUTH_CLIENT_ID)
        .requestEmail()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        ViewBinding
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        Google SignIN Client
        googleSignInClient = GoogleSignIn.getClient(application, gso)


//        ViewModel
        viewModel = ViewModelProvider(this).get(SignInViewModel::class.java)


//        Livedata
        viewModel.snack.observe(this, Observer { text ->
            text?.let { binding.root.makeSimpleSnack(it) }
        })
        viewModel.uiState.observe(this, Observer { state ->
            state?.let {
                updateUiState(it)
            }
        })
        viewModel.canNavigateToApp.observe(this, Observer {
            it.let {
                if (it) navigateToApp()
            }
        })


//        Button Listeners
        binding.buttonNewUser.setOnClickListener {
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }

        binding.buttonSignIn.setOnClickListener {
            it.hideSoftKeyboard()
            if (binding.editTextEmailSignIn.validEmail() &&
                binding.editTextPasswordSignIn.validatePasswordSuccessful()
            ) {
                viewModel.signInWithEmailAndPassword(
                    email = binding.editTextEmailSignIn.text.toString(),
                    password = binding.editTextPasswordSignIn.text.toString()
                )
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val i = Intent(this, ForgotPasswordActivity::class.java )
            startActivity(i)
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_RC_SIGN_IN)
        }

    }

    //  Activity result for google sign in
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                viewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    //  Toggle between Loading and active screens
    private fun updateUiState(state: UiState) {
        if (state == UiState.LOADING) {
            binding.loadingView.visibility = View.VISIBLE
            binding.editTextEmailSignIn.isFocusable = false
            binding.editTextPasswordSignIn.isFocusable = false
            binding.tvForgotPassword.isFocusable = false

        } else if (state == UiState.ACTIVE) {
            binding.loadingView.visibility = View.GONE
            binding.editTextEmailSignIn.isFocusable = true
            binding.editTextPasswordSignIn.isFocusable = true
            binding.tvForgotPassword.isFocusable = true
        }
    }


    private fun navigateToApp() {
        val intent = Intent(this, VerifyUserActivity::class.java)
        startActivity(intent)
        finish()
    }


}