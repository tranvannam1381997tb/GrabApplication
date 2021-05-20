package com.example.grabapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookInfo(
    var driverId: String,
    var tokenId: String,
    var name: String,
    var age: Int,
    var sex: String,
    var phoneNumber: String,
    var startAddress: String,
    var endAddress: String,
    var latStart: Double,
    var lngStart: Double,
    var latEnd: Double,
    var lngEnd: Double,
    var price: String,
    var distance: String

): Parcelable