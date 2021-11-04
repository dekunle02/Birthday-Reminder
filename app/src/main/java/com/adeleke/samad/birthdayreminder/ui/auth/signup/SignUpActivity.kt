package com.adeleke.samad.birthdayreminder.ui.auth.signup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.ActivitySignUpBinding
import com.adeleke.samad.birthdayreminder.ui.auth.VerifyUserActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    // Configure Google Sign In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(OAUTH_CLIENT_ID)
        .requestEmail()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      ViewBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        Set up the toolbar up button
        setSupportActionBar(binding.toolbarSignUp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


//        Google SignIN Client
        googleSignInClient = GoogleSignIn.getClient(application, gso)



//       ViewModel Init
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

//      Livedata
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

//        Click Listeners
        binding.buttonOldUser.setOnClickListener {
            onBackPressed()
        }

        binding.buttonSignUp.setOnClickListener {
            it.hideSoftKeyboard()
            if (binding.editTextEmailSignUp.validEmail() &&
                binding.editTextPasswordSignUp.validatePasswordSuccessful() &&
                        binding.editTextPassword2SignUp.validatePasswordSuccessful()
            ) {
                if (binding.editTextPasswordSignUp.text.toString().equals(binding.editTextPassword2SignUp.text.toString())) {
                    viewModel.signUpWithEmailAndPassword(
                        email = binding.editTextEmailSignUp.text.toString(),
                        password = binding.editTextPasswordSignUp.text.toString(),
                        name = binding.editTextNameSignUp.text.toString()
                    )
                } else {
                    it.makeSimpleSnack("Passwords should be similar")
                }
            }
        }

        binding.buttonGoogleSignUp.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_RC_SIGN_IN)
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1UbU3zE_8wgg9T1zbZ1ZuVpbwBqsMha7z/view?usp=sharing"))
            startActivity(browserIntent)
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

    private fun navigateToApp() {
        val intent = Intent(this, VerifyUserActivity::class.java)
        startActivity(intent)
        finish()
    }

    //  Toggle between Loading and active screens
    private fun updateUiState(state: UiState) {
        if (state == UiState.LOADING) {
            binding.loadingView.visibility = View.VISIBLE
            binding.editTextEmailSignUp.isFocusable = false
            binding.editTextPasswordSignUp.isFocusable = false
            binding.editTextPassword2SignUp.isFocusable = false
            binding.editTextNameSignUp.isFocusable = false

        } else if (state == UiState.ACTIVE) {
            binding.loadingView.visibility = View.GONE
            binding.editTextEmailSignUp.isFocusable = true
            binding.editTextPasswordSignUp.isFocusable = true
            binding.editTextPassword2SignUp.isFocusable = true
            binding.editTextNameSignUp.isFocusable = true
        }
    }

}