package com.example.grabapplication.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.common.Constants
import com.example.grabapplication.model.TYPE_GRAB_BIKE
import com.example.grabapplication.model.TYPE_GRAB_CAR
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import java.time.format.DateTimeFormatter

class FirebaseUtils {
    companion object {

        fun getDoubleFromDataSnapshot(snapshot: DataSnapshot, key: String): Double {
            return (snapshot.child(key).getValue(Double::class.java)) ?: (-1).toDouble()
        }

        fun getIntFromDataSnapshot(snapshot: DataSnapshot, key: String): Int {
            return (snapshot.child(key).getValue(Int::class.java)) ?: -1
        }

        fun getStringFromDataSnapshot(snapshot: DataSnapshot, key: String): String {
            return (snapshot.child(key).getValue(String()::class.java)) ?: ""
        }

        fun getDateFromDataSnapshot(snapshot: DataSnapshot, key: String): String {
            val dateString = (snapshot.child(key).getValue(String()::class.java)) ?: ""
            return convertStringToDateFirebase(dateString)
        }

        fun getTypeDriverFromDataSnapshot(snapshot: DataSnapshot, key: String): String {
            val typeDriver = snapshot.child(key).getValue(Int::class.java)
            if (typeDriver == TYPE_GRAB_BIKE) {
                return GrabApplication.getAppContext().getString(R.string.grab_bike)
            }
            if (typeDriver == TYPE_GRAB_CAR) {
                return GrabApplication.getAppContext().getString(R.string.grab_car)
            }
            return ""
        }

        @SuppressLint("SimpleDateFormat")
        fun convertStringToDateFirebase(dateString: String): String {
            try {
                val formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_FOR_FIREBASE)
                val date = formatter.parse(dateString)

                return DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_APP).format(date)
            } catch (e: Exception) {
                Log.d("NamTV", "CommonUtils::convertStringToDate: exception = $e")
            }

            return ""
        }
    }
}