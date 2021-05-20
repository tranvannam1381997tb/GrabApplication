package com.example.grabapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.properties.Delegates

@Parcelize
data class UserInfo(
    var userId: String,
    var tokenId: String,
    var name: String,
    var age: Int,
    var sex: Int,
    var phoneNumber: String,
    var latitude: Double,
    var longitude: Double,
    var status: Int
): Parcelable

enum class UserInfoKey(val rawValue: String) {
    KeyUser("user"),
    KeyUserId("userId"),
    KeyTokenId("tokenId"),
    KeyName("name"),
    KeyAge("age"),
    KeySex("sex"),
    KeyPhoneNumber("phoneNumber"),
    KeyPassword("password"),
    KeyLatitude("latitude"),
    KeyLongitude("longitude"),
    KeyStatus("status"),
    KeyStartDate("startDate")
}

enum class SexValue(val rawValue: String) {
    MALE("Nam"),
    FEMALE("Nữ")
}