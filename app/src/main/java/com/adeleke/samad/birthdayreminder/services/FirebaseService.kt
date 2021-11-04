package com.adeleke.samad.birthdayreminder.services

import android.graphics.Bitmap
import android.util.Log
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.model.Birthday
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseService {

    companion object {
        const val TAG = "FirebaseService"


        suspend fun fetchAllBirthdays(): List<Birthday?> =
            suspendCoroutine {
                val currentUserId = Firebase.auth.currentUser?.uid
                val databaseRef =
                    currentUserId?.let {
                        Firebase.database.reference.child(BIRTHDAY_REFERENCE).child(it)
                    }
                val callback =
                    databaseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val birthdayList = mutableListOf<Birthday?>()
                            for (singleSnapshot in snapshot.children) {
                                val birthday = singleSnapshot.getValue<Birthday>()
                                birthday?.let { birthdayList.add(it) }
                            }
                            it.resume(birthdayList.sortByDate().filterNot { it?.archived == true })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "onCancelled: Error fetching list ${error.message}")
                            it.resume(listOf())
                        }
                    })
            }


        suspend fun uploadProfilePhoto(bitmap: Bitmap): ServiceResponse<String> =
            suspendCoroutine { cont ->
                val currentUserId = Firebase.auth.currentUser?.uid

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val storageRef: StorageReference = FirebaseStorage.getInstance().getReference(
                    PROFILE_PICTURES
                ).child("$currentUserId.jpg")

                val callbackTask = storageRef.putBytes(data)
                callbackTask.addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        cont.resume(ServiceResponse.Success(imageUrl))
                    }
                }
                    .addOnFailureListener {
                        cont.resume(ServiceResponse.Failure(it))
                    }
            }

        suspend fun uploadBirthdayPicture(
            bitmap: Bitmap?,
            birthday: Birthday
        ): ServiceResponse<String> =
            suspendCoroutine { cont ->

                val currentUserId = Firebase.auth.currentUser?.uid
                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val birthdayId = birthday.id
                val storageRef: StorageReference = FirebaseStorage.getInstance().getReference(
                    BIRTHDAY_PICTURES
                ).child(currentUserId!!).child("$birthdayId.jpg")

                val uploadTask = storageRef.putBytes(data)
                uploadTask.addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        cont.resume(ServiceResponse.Success(imageUrl))
                    }
                }
                    .addOnFailureListener {
                        cont.resume(ServiceResponse.Failure(it))
                    }
            }

        suspend fun fetchProfilePhotoUrl(): ServiceResponse<String> =
            suspendCoroutine { cont ->
                val currentUserId = Firebase.auth.currentUser?.uid
                val storageRef: StorageReference = FirebaseStorage.getInstance().getReference(
                    PROFILE_PICTURES
                ).child("$currentUserId.jpg")

                val task = storageRef.downloadUrl.addOnSuccessListener {
                    cont.resume(ServiceResponse.Success(it.toString()))
                }.addOnFailureListener {
                    cont.resume(ServiceResponse.Failure(it))
                }
            }

        suspend fun saveOrUpdateBirthday(
            birthday: Birthday,
        ): ServiceResponse<String> =
            suspendCoroutine { cont ->
                val currentUserId = Firebase.auth.currentUser?.uid
                val birthdayId = birthday.id
                val databaseRef =
                    currentUserId?.let {
                        birthdayId?.let {
                            Firebase.database.reference.child(BIRTHDAY_REFERENCE)
                                .child(currentUserId)
                                .child(birthdayId)
                        }
                    }
                databaseRef?.setValue(birthday)?.addOnSuccessListener {
                    cont.resume(ServiceResponse.Success("Birthday successfully uploaded"))
                }
                    ?.addOnFailureListener {
                        cont.resume(ServiceResponse.Failure(it))
                    }
            }
    }


}