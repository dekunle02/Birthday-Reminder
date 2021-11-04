package com.adeleke.samad.birthdayreminder.ui.birthdayDetail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.ActivityBirthdayDetailBinding
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.adeleke.samad.birthdayreminder.ui.birthdayEdit.BirthdayEditActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class BirthdayDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: BirthdayDetailViewModel
    private lateinit var binding: ActivityBirthdayDetailBinding
    val context = this

    private var mBirthday: Birthday? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  ViewBinding
        binding = ActivityBirthdayDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //  Set up the toolbar up button
        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //  Receive BirthdayId from Intent
        val intent = intent
        val birthdayId = intent.getStringExtra(BIRTHDAY_DETAIL_INTENT_KEY)

        //  ViewModel
        if (birthdayId == null) {
            updateUiState(UiState.ERROR)
            onBackPressed()
        } else {
            updateUiState(UiState.LOADING)
            val viewModelFactory =
                BirthdayDetailViewModel.BirthdayDetailViewModelFactory(birthdayId)
            viewModel =
                ViewModelProvider(this, viewModelFactory).get(BirthdayDetailViewModel::class.java)
        }

        //  LiveData
        viewModel.snack.observe(this, Observer { text ->
            text?.let { binding.root.makeSimpleSnack(it) }
        })
        viewModel.uiState.observe(this, Observer { state ->
            state?.let {
                updateUiState(it)
            }
        })
        viewModel.birthday.observe(this, Observer { birthday ->
            birthday?.let {
                mBirthday = it
                displayBirthdayInfo(it)
            }
        })
        viewModel.canCloseActivity.observe(this, Observer {
            it?.let { canClose ->
                Log.d(TAG, "onCreate: canClose activity is changed to $canClose")
                if (canClose) finish()
            }
        })

        //  Button Listeners
        binding.fabEdit.setOnClickListener {
            val i = Intent(context, BirthdayEditActivity::class.java)
            i.putExtra(BIRTHDAY_IS_NEW_KEY, false)
            i.putExtra(BIRTHDAY_EDIT_INTENT_KEY, birthdayId)
            i.putExtra(IMPORT_CONTACT_INTENT_KEY, false)
            context.startActivity(i)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.birthday_detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        //      Set up the toolbar icons and buttons
        val archiveMenu = menu?.findItem(R.id.action_archive_birthday)
        val favoriteMenu = menu?.findItem(R.id.action_mark_favorite)

        if (mBirthday?.archived == true) {
            archiveMenu?.title = "Unarchive"
            archiveMenu?.setOnMenuItemClickListener { it ->
                viewModel.restore()
                it.title = "Archive"
                true
            }
        } else {
            archiveMenu?.title = "Archive"
            archiveMenu?.setOnMenuItemClickListener { it ->
                viewModel.archive();
                it.title = "UnArchive"
                true
            }
        }
        if (mBirthday?.favorite == true) {
            favoriteMenu?.setIcon(R.drawable.ic_favorite_filled)
            favoriteMenu?.setOnMenuItemClickListener { it ->
                viewModel.unFavorite()
                it.setIcon(R.drawable.ic_favorite_border)
                true
            }
        } else {
            favoriteMenu?.setIcon(R.drawable.ic_favorite_border)
            favoriteMenu?.setOnMenuItemClickListener { it ->
                viewModel.favorite()
                it.setIcon(R.drawable.ic_favorite_filled)
                true
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_delete_birthday -> {
                MaterialAlertDialogBuilder(this.context)
                    .setMessage(getString(R.string.delete_prompt))
                    .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                        // Respond to negative button press
                    }
                    .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                        viewModel.delete()
                    }
                    .show()
                true
            }

            R.id.action_share_birthday -> {
                val shareMessage = viewModel.birthday.value?.shareMessage()
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    shareMessage
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayBirthdayInfo(birthday: Birthday) {
        binding.tvDetailName.text = birthday.name
        binding.tvDetailDate.text = birthday.date().toFormattedString()
        binding.tvDetailPhone.text = birthday.phoneNumber


        //      Set up the text fields
        birthday.email?.let {
            binding.tvDetailEmail.text = it
        } ?: run { binding.tvDetailEmail.visibility = View.GONE }

        birthday.message?.let {
            binding.tvDetailMessage.text = it
        } ?: run {
            binding.tvDetailMessage.text = getString(R.string.no_message_set)
            binding.tvDetailMessage.setTextAppearance(R.style.TextViewNoContent)
        }

        birthday.notes?.let {
            binding.tvDetailNote.text = it
        } ?: run {
            binding.tvDetailNote.text = getString(R.string.no_notes_set)
            binding.tvDetailNote.setTextAppearance(R.style.TextViewNoContent)
        }

        birthday.ageString()?.let {
            binding.tvDetailAge.text = it
        } ?: run {
            binding.tvDetailAge.visibility = View.GONE
        }

        // Set up the picture
        Glide
            .with(this)
            .load(birthday.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_face)
            .into(binding.ivDetailAvatar)

    }

    private fun updateUiState(uiState: UiState) {
        when (uiState) {
            UiState.ACTIVE -> {
                binding.progressBarDetail.visibility = View.GONE
                binding.scrollViewDetail.visibility = View.VISIBLE
                binding.fabEdit.isFocusable = true
            }
            UiState.LOADING -> {
                binding.progressBarDetail.visibility = View.VISIBLE
                binding.scrollViewDetail.visibility = View.GONE
                binding.fabEdit.isFocusable = false
            }
            else -> {
                binding.progressBarDetail.visibility = View.GONE
                binding.scrollViewDetail.visibility = View.GONE
                binding.fabEdit.isFocusable = false
            }
        }
    }

    companion object {
        const val TAG = "BirthdayDetailActivity"
    }
}