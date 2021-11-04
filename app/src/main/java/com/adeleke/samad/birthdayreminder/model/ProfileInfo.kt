package com.adeleke.samad.birthdayreminder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProfileInfo(
    val userId: String,
    var displayName: String,
    var imageUrl: String
) : Parcelable {
    companion object {

    }
}
