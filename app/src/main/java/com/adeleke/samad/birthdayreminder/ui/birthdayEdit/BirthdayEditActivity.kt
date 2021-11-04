package com.adeleke.samad.birthdayreminder.ui.birthdayEdit

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.ActivityBirthdayEditBinding
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.time.LocalDate


class BirthdayEditActivity : AppCompatActivity() {

    private lateinit var viewModel: BirthdayEditViewModel
    private lateinit var binding: ActivityBirthdayEditBinding

    private val activityReference = this
    private var hasMadeChanges: Boolean = false
    private var shouldImportContact: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //        ViewBinding
        binding = ActivityBirthdayEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //          ViewModel
        viewModel = ViewModelProvider(this).get(BirthdayEditViewModel::class.java)


        //      Receive Birthday Information from Intent
        val birthdayNew = intent.getBooleanExtra(BIRTHDAY_IS_NEW_KEY, true)
        val birthdayId = intent.getStringExtra(BIRTHDAY_EDIT_INTENT_KEY)
        shouldImportContact = intent.getBooleanExtra(IMPORT_CONTACT_INTENT_KEY, false)


        //      if asked to Import Contact through intent
        if (shouldImportContact) dispatchImportContactIntent()

        //      Configure toolbar text
        //        Set up the toolbar up button
        setSupportActionBar(binding.tbEdit)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (birthdayNew) "Add Birthday" else "Edit Birthday"

        if (birthdayId != null && !birthdayNew) {
            viewModel.retrieveBirthdayWithId(birthdayId)
        } else {
            viewModel.makeNewBirthday()
        }

        //        LiveData
        viewModel.snack.observe(this, Observer { text ->
            text?.let { binding.root.makeSimpleSnack(it) }
        })
        viewModel.birthday.observe(this, Observer { birthday ->
            birthday?.let { it ->
                populateFieldsWithBirthday(it)
            }
        })
        viewModel.canNavigateBack.observe(this, Observer { it ->
            it?.let {
                onBackPressed()
            }
        })
        viewModel.uiState.observe(this, Observer {
            it?.let {
                updateUiState(it)
            }
        })

        // Set up DatePicker listener
        binding.editDatePicker.setOnDateChangedListener { datePicker, year, monthOfYear, dayOfMonth ->
            val today = LocalDate.now()
            val selectedDate =
                today.withYear(year).withMonth(monthOfYear + 1).withDayOfMonth(dayOfMonth)
            binding.tfEditDate.editText?.setText(selectedDate.toFormattedString())
            hasMadeChanges = true
        }

//                  Click Listeners
        binding.ivEditAvatar.setOnClickListener {
            showImageSelectionDialog()
        }

        //  Set up field awareness
        setUpFieldAwareness()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.birthday_edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: Has made changes ->$hasMadeChanges")
                if (hasMadeChanges || shouldImportContact){
                    MaterialAlertDialogBuilder(this.activityReference)
                        .setMessage(getString(R.string.discard_prompt))

                        .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                            // Respond to negative button press
                        }
                        .setPositiveButton(getString(R.string.discard)) { dialog, which ->
                            viewModel.discardChanges()
                            onBackPressed()
                        }
                        .show()
                }else {
                    onBackPressed()
                }
                true
            }

            R.id.action_delete -> {
                MaterialAlertDialogBuilder(this.activityReference)
                    .setMessage(getString(R.string.delete_prompt))
                    .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                        // Respond to negative button press
                    }
                    .setPositiveButton(getString(R.string.delete)) { dialog, which ->
                        viewModel.deleteBirthday()
                        onBackPressed()
                    }
                    .show()
                true
            }

            R.id.action_save -> {
                saveOrUpdateBirthdayFields()
                true
            }

            R.id.action_import_from_contact -> {
                MaterialAlertDialogBuilder(this.activityReference)
                    .setTitle(resources.getString(R.string.import_contact))
                    .setMessage(getString(R.string.import_contact_body))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        // Dismiss
                    }
                    .setNegativeButton(getString(R.string.decline)) { dialog, which ->
                        // Dismiss
                    }
                    .setPositiveButton(getString(R.string.accept)) { dialog, which ->
                        dispatchImportContactIntent()
                    }
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    //          The Activity result that receives chosen contact and passes it to the viewModel
    //          Also receives the chosen image from the gallery and or camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
            val contactUri = data?.data
            contactUri?.let { populateFieldsWithContact(it) }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                viewModel.changeCurrentBitmap(it)
                Glide.with(this)
                    .load(imageBitmap)
                    .into(binding.ivEditAvatar);
                binding.ivEditAvatar.setImageBitmap(imageBitmap)
                hasMadeChanges= true
            }
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    val imageStream = contentResolver.openInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(imageStream)
                    viewModel.changeCurrentBitmap(imageBitmap)
                    Glide.with(this)
                        .load(imageBitmap)
                        .into(binding.ivEditAvatar);
                    binding.ivEditAvatar.setImageBitmap(imageBitmap)
                    hasMadeChanges= true
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                binding.root.makeSimpleSnack("Oops..something went wrong")
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CONTACT_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivityForResult(intent, CONTACT_REQUEST_CODE)
                    }

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    binding.root.makeSimpleSnack("We need your permission to read your contacts.")
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    //          Setting up Key Pressing to detect changes
    private fun setUpFieldAwareness() {
        binding.tfEditEmail.editText?.setOnKeyListener { p0, p1, p2 ->
            hasMadeChanges = true
            true
        }
        binding.tfEditName.editText?.setOnKeyListener { p0, p1, p2 ->
            hasMadeChanges = true
            true
        }
        // This somehow makes it impossible to make changes to the telephone number
//        binding.tfEditPhone.editText?.setOnKeyListener { p0, p1, p2 ->
//            hasMadeChanges = true
//            true
//        }
        binding.tfEditDate.editText?.setOnKeyListener { p0, p1, p2 ->
            hasMadeChanges = true
            true
        }
        binding.tfEditNote.editText?.setOnKeyListener { p0, p1, p2 ->
            hasMadeChanges = true
            true
        }
        binding.tfEditMessage.editText?.setOnKeyListener { p0, p1, p2 ->
            hasMadeChanges = true
            true
        }
    }


    //          Populating fields
    private fun populateFieldsWithContact(contactUri: Uri) {
        var contactId: String? = null
        var contactIdLong: Long? = null

        val projection: Array<String> = arrayOf(
            ContactsContract.Data._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.DISPLAY_NAME,
        )

        val contactCursor: Cursor? = this.contentResolver.query(
            contactUri,
            projection,
            null,
            null,
            null
        )
        if (contactCursor != null && contactCursor.moveToFirst()) {

            val numberIndex =
                contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val number = contactCursor.getString(numberIndex)
            binding.tfEditPhone.editText?.setText(number)

            val nameIndex = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val name = contactCursor.getString(nameIndex)
            binding.tfEditName.editText?.setText(name)

            val contactIndex = contactCursor.getColumnIndex(ContactsContract.Contacts._ID)
            contactId = contactCursor.getString(contactIndex)
            contactIdLong = contactCursor.getLong(contactIndex)
            val emailCursor: Cursor? = this.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                null,
                null
            )
            if (emailCursor != null && emailCursor.moveToFirst()) {
                try {
                    val emailIndex =
                        emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                    val email = emailCursor.getString(emailIndex)
                    binding.tfEditEmail.editText?.setText(email)
                } catch (e: Exception) {
                    binding.tfEditEmail.editText?.setText("")
                }
            } else {
            }
            emailCursor?.close()
        }
        contactCursor?.close()

        val imageInputStream = contactIdLong?.let { openPhoto(it) }
        imageInputStream?.let {
            val imageBitmap = BitmapFactory.decodeStream(imageInputStream)
            Log.d(TAG, "populateFieldsWithContact: ImageBitmapMade")
            binding.ivEditAvatar.setImageBitmap(imageBitmap)
//            viewModel.uploadPhoto(it)
        } ?: run {
            Log.d(TAG, "populateFieldsWithContact: ImageINputStream is null")
        }

    }

    private fun populateFieldsWithBirthday(birthday: Birthday) {

        Glide
            .with(this)
            .load(birthday.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_add_a_photo)
            .into(binding.ivEditAvatar)

        binding.tfEditName.editText?.setText(birthday.name)
        binding.tfEditPhone.editText?.setText(birthday.phoneNumber)
        binding.tfEditEmail.editText?.setText(birthday.email)

        binding.editDatePicker.updateDate(birthday.yearOfBirth, birthday.monthOfBirth - 1, birthday.dayOfBirth)
        binding.tfEditDate.editText?.setText(birthday.date().toFormattedString())

        binding.tfEditMessage.editText?.setText(birthday.message)
        binding.tfEditNote.editText?.setText(birthday.notes)


    }


    //          Image Selection
    private fun showImageSelectionDialog() {
        val items = arrayOf("Take photo", "Choose photo")

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.change_photo))
            .setItems(items) { dialog, which ->
                // Respond to item chosen
                when (which) {
                    0 -> {
                        dispatchCameraIntent()
                    }
                    1 -> {
                        dispatchGalleryIntent()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                // Removes popUp
            }
            .show()
    }

    private fun dispatchGalleryIntent() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST_CODE)
    }

    private fun dispatchCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            binding.root.makeSimpleSnack("An error occurred.")
        }
    }


    //          Contact selection
    private fun dispatchImportContactIntent() {

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivityForResult(intent, CONTACT_REQUEST_CODE)
                    }
                } else {
                    binding.root.makeSimpleSnack("We need your permission to get access to your contacts.")
                }
            }

        //      Check for Permissions Present
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, CONTACT_REQUEST_CODE)
            }
        } else {

//            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
//                CONTACT_PERMISSION_REQUEST_CODE)
            requestPermissionLauncher.launch(
                Manifest.permission.READ_CONTACTS
            )
        }

    }

    private fun openPhoto(contactId: Long): InputStream? {
        val contactUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactId
        )
        val photoUri = Uri.withAppendedPath(
            contactUri,
            ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
        )
        Log.d(TAG, "openPhoto: photoURI ${photoUri.toString()}")
        val cursor =
            contentResolver.query(
                photoUri,
                arrayOf(ContactsContract.Contacts.Photo.PHOTO),
                null,
                null,
                null
            )
                ?: return null
        try {
            if (cursor.moveToFirst()) {
                val data = cursor.getBlob(0)
                Log.d(TAG, "openPhoto: data is null? ${data.isEmpty()}")
                if (data != null) {
                    return ByteArrayInputStream(data)
                }
            }
        } finally {
            cursor.close()
        }
        return null
    }


    //          Saving Birthday Using it's fields
    private fun saveOrUpdateBirthdayFields() {
        var email = ""
        if (binding.tfEditEmail.editText?.text.toString().isNotEmpty()) {
            email = binding.tfEditEmail.editText?.cleanEmail() ?: return
        }

        val name: String = binding.tfEditName.editText?.cleanName() ?: return
        val phoneNumber: String = binding.tfEditPhone.editText?.text.toString()
        val date: LocalDate = binding.tfEditDate.editText?.text.toString().toLocalDate()
        val dayOfMonth: Int = date.dayOfMonth
        val monthOfYear: Int = date.monthValue
        val year: Int = date.year
        val message: String = binding.tfEditMessage.editText?.text.toString()
        val notes: String = binding.tfEditNote.editText?.text.toString()

        val temporaryBirthday = Birthday(
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            dayOfBirth = dayOfMonth,
            monthOfBirth = monthOfYear,
            yearOfBirth = year,
            message = message,
            notes = notes
        )

        viewModel.saveBirthday(temporaryBirthday)
    }


    //          Ui State Toggling
    private fun updateUiState(uiState: UiState) {
        when (uiState) {
            UiState.ACTIVE -> {
                binding.progressBarEdit.visibility = View.GONE
                binding.nestedScrollViewEdit.visibility = View.VISIBLE
            }
            UiState.LOADING -> {
                binding.progressBarEdit.visibility = View.VISIBLE
                binding.nestedScrollViewEdit.visibility = View.GONE
            }
            else -> {
                binding.progressBarEdit.visibility = View.GONE
                binding.nestedScrollViewEdit.visibility = View.GONE
                onBackPressed()
                finish()
            }
        }
    }

    companion object {
        const val TAG = "BirthdayEditActivity"
    }
}