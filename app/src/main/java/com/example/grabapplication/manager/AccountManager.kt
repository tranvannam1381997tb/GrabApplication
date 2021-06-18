package com.example.grabapplication.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.common.AppPreferences
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.common.Constants
import com.example.grabapplication.connection.HttpConnection
import com.example.grabapplication.firebase.FirebaseManager
import com.example.grabapplication.services.GetPolicyReceiver
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*


class AccountManager private constructor() {

    private var userId: String? = null
    private var tokenId: String? = null
    private var name: String? = null
    private var age: Int? = null
    private var sex: String? = null
    private var phoneNumber: String? = null
    private var currentLocation: LatLng? = null
    private var status: Int? = null

    companion object {
        const val KEY_POLICY = "policy"
        const val KEY_POINT = "point"


        private var instance: AccountManager? = null

        fun getInstance(): AccountManager {
            if (instance == null) {
                synchronized(AccountManager::class.java) {
                    if (instance == null) {
                        instance = AccountManager()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        getTokenIdDevice {
            tokenId = it
        }
    }

    fun saveUserId(id: String) {
        userId = id
    }

    fun getUserId(): String{
        return userId ?: ""
    }

    fun getTokenIdDevice(callback: (String?) -> Unit) {
        if (tokenId != null) {
            callback.invoke(tokenId)
            FirebaseManager.getInstance().updateTokenIdToFirebase(tokenId!!)
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = it.result
            if (token != null) {
                Log.d("NamTV", "$token")
                callback.invoke(token)
                FirebaseManager.getInstance().updateTokenIdToFirebase(token)
            }
        }
    }

    fun setLocationUser(location: LatLng) {
        currentLocation = location
        FirebaseManager.getInstance().updateLocationUserToFirebase(location)
    }

    fun getLocationUser(): LatLng {
        return currentLocation ?: Constants.DEFAULT_LOCATION
    }

    fun setUserInfo(name: String, age: Int, sex: String, phoneNumber: String, status: Int) {
        this.name = name
        this.age = age
        this.sex = sex
        this.phoneNumber = phoneNumber
        this.status = status
    }

    fun getTokenId(): String {
        if (tokenId == null) {
            getTokenIdDevice { tokenId = it }
            return ""
        }
        return tokenId!!
    }

    fun getName(): String {
        return name!!
    }

    fun getSex(): String {
        return sex!!
    }

    fun getAge(): Int {
        return age!!
    }

    fun getPhoneNumber(): String {
        return phoneNumber!!
    }

    fun getPolicy() {
        HttpConnection.getInstance().getPolicy { isSuccess, jsonData ->
            if (isSuccess) {
                val jsonPolicy = CommonUtils.getJsonObjectFromJsonObject(jsonData, KEY_POLICY)
                val priceOfKilometer = CommonUtils.getIntFromJsonObject(jsonPolicy, AppPreferences.PRICE_OF_KILOMETER)
                val point = CommonUtils.getJsonObjectFromJsonObject(jsonPolicy, KEY_POINT)
                val ratePoint = CommonUtils.getFloatFromJsonObject(point, AppPreferences.RATE_POINT)
                val distancePoint = CommonUtils.getFloatFromJsonObject(point, AppPreferences.DISTANCE_POINT)
                val agePoint = CommonUtils.getFloatFromJsonObject(point, AppPreferences.AGE_POINT)
                savePolicy(priceOfKilometer, ratePoint, distancePoint, agePoint)
            }
        }
        scheduleUpdatePolicy()
    }

    private fun scheduleUpdatePolicy() {
        GrabApplication.getAppContext().let {
            val getPolicyReceiver = Intent(it, GetPolicyReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(it, Constants.REQUEST_GET_POLICY, getPolicyReceiver, 0)
            val timeNeedUpdatePolicy = getTimeNeedUpdatePolicy()
            val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeNeedUpdatePolicy, pendingIntent)
        }
    }

    private fun savePolicy(priceOfKilometer: Int, ratePoint: Float, distancePoint: Float, agePoint: Float) {
        val appPreferences = AppPreferences.getInstance(GrabApplication.getAppContext())
        appPreferences.priceOfKilometer = priceOfKilometer
        appPreferences.ratePoint = ratePoint
        appPreferences.distancePoint = distancePoint
        appPreferences.agePoint = agePoint
    }

    private fun getTimeNeedUpdatePolicy(): Long {
        val currentHourIn24Format: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeUpdatePolicy = Calendar.getInstance()
        when (currentHourIn24Format) {
            in 0..5 -> {
                //0h-5h
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 6)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            6 -> {
                //6h
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 7)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            in 7..9 -> {
                //7h-9h
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 10)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            in 10..16 -> {
                //10h-16h
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 17)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            in 17..19 -> {
                //17h-19h
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 20)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            in 20..21 -> {
                //20h-21h
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 22)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            in 22..23 -> {
                //22h-23h
                timeUpdatePolicy.add(Calendar.DATE, 1)
                timeUpdatePolicy.set(Calendar.HOUR_OF_DAY, 6)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
            else -> {
                timeUpdatePolicy.add(Calendar.DATE, 1)
                timeUpdatePolicy.set(Calendar.MINUTE, 0)
            }
        }
        Log.d("NamTV", "GetPolicyReceiver get policy at ${timeUpdatePolicy.time}")
        return timeUpdatePolicy.timeInMillis
    }
}

