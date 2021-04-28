package com.example.grabapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.properties.Delegates

@Parcelize
data class UserInfo(
    var idUser: String,
    var tokenId: String,
    var name: String,
    var age: Int,
    var sex: Int,
    var phoneNumber: String,
    var latitude: Double,
    var longitude: Double,
    var status: Int
): Parcelable