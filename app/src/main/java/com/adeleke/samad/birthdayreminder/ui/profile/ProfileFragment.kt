package com.adeleke.samad.birthdayreminder.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.ProfileFragmentBinding
import com.adeleke.samad.birthdayreminder.model.ProfileInfo
import com.adeleke.samad.birthdayreminder.ui.auth.ResetPasswordActivity
import com.adeleke.samad.birthdayreminder.ui.auth.signin.SignInActivity
import com.adeleke.samad.birthdayreminder.ui.birthdayDetail.BirthdayDetailViewModel
import com.adeleke.samad.birthdayreminder.ui.settings.SettingsActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.io.FileNotFoundException

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        Binding and ViewModel
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)


//        LiveData
        viewModel.snack.observe(viewLifecycleOwner, Observer { text ->
            text?.let { binding?.root?.makeSimpleSnack(it) }
        })
        viewModel.profileInfo.observe(viewLifecycleOwner, Observer { p ->
            p?.let { showProfileInfo(p) }
        })

//         onClickListeners
        binding?.ivUserAvatar?.setOnClickListener {
            showImageSelectionDialog()
        }
        binding?.btnProfileLogOut?.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(activity, SignInActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
        binding?.btnChangeName?.setOnClickListener {
            launchNameChangeDialog(it)
        }
        binding?.btnProfileAbout?.setOnClickListener {
            launchAboutDialog()
        }
        binding?.btnProfilePassword?.setOnClickListener {
            val i = Intent(requireActivity(), ResetPasswordActivity::class.java)
            startActivity(i)
        }
        binding?.btnProfilePrivacy?.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1UbU3zE_8wgg9T1zbZ1ZuVpbwBqsMha7z/view?usp=sharing"))
            startActivity(browserIntent)
        }
        binding?.btnProfileSetting?.setOnClickListener {
            val i = Intent(requireActivity(), SettingsActivity::class.java)
            startActivity(i)
        }



        return binding?.root
    }

    // Pop ups
    private fun launchNameChangeDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(view.context).inflate(R.layout.dialog_name_change, viewGroup, false)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        val btnConfirmSubmit: MaterialButton = dialogView.findViewById(R.id.btn_change_name_ok)
        val btnCancel: MaterialButton = dialogView.findViewById(R.id.btn_change_name_cancel)
        btnCancel.setOnClickListener { alertDialog.dismiss() }
        btnConfirmSubmit.setOnClickListener {
            val newName = (dialogView.findViewById(R.id.et_new_name) as EditText).text.toString()
            viewModel.setNewProfileName(newName)
        }
    }

    private fun launchAboutDialog() {
        val versionCode = BuildConfig.VERSION_CODE
        val aboutMessage = "Version- $versionCode\nMade by Samad Adeleke (dekunle02@gmail.com)"
        context?.let {

            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.about_head))
                .setMessage(aboutMessage)
                .setPositiveButton(resources.getString(R.string.done)) { dialog, which ->
                    // Respond to positive button press
                }
                .show()
        }
    }


    private fun showProfileInfo(profileInfo: ProfileInfo) {
        binding?.tvProfileName?.text = profileInfo.displayName
        binding?.ivUserAvatar?.let {
            Glide
                .with(requireActivity())
                .load(profileInfo.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_face)
                .into(it)
        }
    }


    //          Image Selection
    private fun showImageSelectionDialog() {
        val items = arrayOf("Take photo", "Choose photo")

        MaterialAlertDialogBuilder(requireContext())
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
            binding?.root?.makeSimpleSnack("An error occurred.")
        }
    }


    //  Receive the image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            binding?.ivUserAvatar?.setImageBitmap(imageBitmap)
            viewModel.uploadProfilePicture(imageBitmap)

        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            try {
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    val imageStream = activity?.contentResolver?.openInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(imageStream)
                    viewModel.uploadProfilePicture(imageBitmap)
                    binding?.ivUserAvatar?.setImageBitmap(imageBitmap)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                binding?.root?.makeSimpleSnack("Oops..something went wrong")
            }
        }
    }


    companion object {
        fun newInstance() = ProfileFragment()
        val TAG = "ProfileFragment"
    }
}