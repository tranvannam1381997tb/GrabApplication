package com.example.grabapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class DriverInfo(
    var idDriver: String,
    var tokenId: String,
    var name: String,
    var age: Int,
    var sex: Int,
    var phoneNumber: String,
    var latitude: Double,
    var longitude: Double,
    var rate: Double,
    var status: Int,
    var startDate: String,
    var typeDriver: String,
    var typeVehicle: String,
    var licensePlateNumber: String
) : Parcelable

enum class DriverInfoKey(val rawValue: String) {
    KeyIdDriver("idDriver"),
    KeyTokenId("tokenId"),
    KeyName("name"),
    KeyAge("age"),
    KeySex("sex"),
    KeyPhoneNumber("phoneNumber"),
    KeyLatitude("latitude"),
    KeyLongitude("longitude"),
    KeyRate("rate"),
    KeyStatus("status"),
    KeyStartDate("startDate"),
    KeyTypeDriver("typeDriver"),
    KeyTypeVehicle("typeVehicle"),
    KeyLicensePlateNumber("licensePlateNumber")
}

const val TYPE_GRAB_BIKE = 1
const val TYPE_GRAB_CAR = 2