package com.example.grabapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookInfo(
    var driverInfo: DriverInfo?,
    var startAddress: String?,
    var endAddress: String?,
    var latStart: Double?,
    var lngStart: Double?,
    var latEnd: Double?,
    var lngEnd: Double?,
    var distance: String?,
    var duration: String?,
    var price: String?

): Parcelable