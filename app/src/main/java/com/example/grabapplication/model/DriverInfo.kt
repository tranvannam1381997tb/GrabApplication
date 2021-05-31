package com.example.grabapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class DriverInfo(
    var driverId: String,
    var tokenId: String,
    var name: String,
    var age: Int,
    var sex: String,
    var phoneNumber: String,
    var latitude: Double,
    var longitude: Double,
    var rate: Float,
    var status: Int,
    var startDate: String,
    var typeDriver: String,
    var typeVehicle: String,
    var licensePlateNumber: String,
    var point: Float
) : Parcelable

enum class DriverInfoKey(val rawValue: String) {
    KeyDriverId("driverId"),
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
    KeyLicensePlateNumber("licensePlateNumber"),
    KeyPoint("point"),
    KeyDistance("distance")
}

enum class DriverStatus(val rawValue: Int) {
    StatusOff(-1),
    StatusOn(0),
    StatusArrivingOrigin(1),
    StatusArrivedOrigin(2),
    StatusArrivingDestination(3),
    StatusArrivedDestination(4)
}

enum class TypeDriverValue(val rawValue: String) {
    GRAB_BIKE("GrabBike"),
    GRAB_CAR("GrabCar")
}