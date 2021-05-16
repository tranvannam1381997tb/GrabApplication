package com.example.grabapplication.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.firebase.FirebaseManager
import com.example.grabapplication.firebase.FirebaseUtils
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.DriverInfoKey
import com.example.grabapplication.services.GetListDriverReceiver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class DriverManager private constructor() {
    var listDriverHashMap: HashMap<String, DriverInfo> = HashMap()
    private var listEventListener: HashMap<String, ValueEventListener> = HashMap()
    private val databaseDrivers = FirebaseManager.getInstance().databaseDrivers

    companion object {
        private var instance: DriverManager? = null

        fun getInstance(): DriverManager {
            if (instance == null) {
                synchronized(DriverManager::class.java) {
                    if (instance == null) {
                        instance = DriverManager()
                    }
                }
            }
            return instance!!
        }
    }

    fun updateDriverInfoFromDataSnapshot(snapshot: DataSnapshot) {
//        val driverId = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyDriverId.rawValue)
        val driverId = snapshot.key
        if (listDriverHashMap[driverId] != null) {
            val driverInfo = listDriverHashMap[driverId]!!
            driverInfo.apply {
                latitude = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyLatitude.rawValue)
                longitude = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyLongitude.rawValue)
                status = FirebaseUtils.getIntFromDataSnapshot(snapshot, DriverInfoKey.KeyStatus.rawValue)
                tokenId = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyTokenId.rawValue)
            }
            listDriverHashMap[driverId!!] = driverInfo
        }
    }

    fun getListDriverFromServer() {
        HttpConnection.getInstance().getListDriver { isSuccess, jsonObject ->
            if (isSuccess) {
                clearAllEventListener()
                getListDriverFromJsonObject(JSONObject(jsonObject))
                getInfoDriver()
            }
        }
        scheduleGetListDriver()
    }

    private fun getInfoDriver() {
        for (driver in listDriverHashMap) {
            val driverInfo = driver.value
            val listener = databaseDrivers.child(driverInfo.driverId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    updateDriverInfoFromDataSnapshot(snapshot)
                    if (listDriverHashMap[driverInfo.driverId] != null) {

                        Log.d("NamTV", "onDataChange ${listDriverHashMap[driverInfo.driverId]!!.latitude} ${listDriverHashMap[driverInfo.driverId]!!.longitude}")
                        MainActivity.addOrUpdateMarkerDriver(listDriverHashMap[driverInfo.driverId]!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("NamTV", "onCancelled")
                }
            })
            listEventListener[driverInfo.driverId] = listener
        }
    }

    private fun clearAllEventListener() {
        for (eventListener in listEventListener) {
            databaseDrivers.removeEventListener(eventListener.value)
        }
    }

    private fun scheduleGetListDriver() {
        GrabApplication.getAppContext().let {
            val keepAliveVPNServiceReceiver = Intent(it, GetListDriverReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(it, Constants.REQUEST_GET_LIST_DRIVER, keepAliveVPNServiceReceiver, 0)
            val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.TIME_SCHEDULE_GET_LIST_DRIVER, pendingIntent)

        }
    }

    private fun getListDriverFromJsonObject(jsonObject: JSONObject) {
        listDriverHashMap.clear()

        val listDriver = CommonUtils.getJsonArrayFromJsonObject(jsonObject, FirebaseConstants.KEY_DRIVERS)
        for (i in 0 until listDriver.length()) {
            val driverJsonObject = listDriver.getJSONObject(i)
            val driverId = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyDriverId.rawValue)
            val tokenId = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyTokenId.rawValue)
            val name =  CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyName.rawValue)
            val age = CommonUtils.getIntFromJsonObject(driverJsonObject, DriverInfoKey.KeyAge.rawValue)
            val sex = CommonUtils.getIntFromJsonObject(driverJsonObject, DriverInfoKey.KeySex.rawValue)
            val phoneNumber = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyPhoneNumber.rawValue)
            val rate = CommonUtils.getDoubleFromJsonObject(driverJsonObject, DriverInfoKey.KeyRate.rawValue)
            val status = CommonUtils.getIntFromJsonObject(driverJsonObject, DriverInfoKey.KeyStatus.rawValue)
            val startDate = CommonUtils.getDateFromJsonObject(driverJsonObject, DriverInfoKey.KeyStartDate.rawValue)
            val typeDriver = CommonUtils.getTypeDriver(driverJsonObject, DriverInfoKey.KeyTypeDriver.rawValue)
            val typeVehicle = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyTypeVehicle.rawValue)
            val licensePlateNumber = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyLicensePlateNumber.rawValue)

            val driverInfo = DriverInfo(
                driverId = driverId,
                tokenId = tokenId,
                name = name,
                age = age,
                sex = sex,
                phoneNumber = phoneNumber,
                latitude = Constants.DEFAULT_LOCATION.latitude,
                longitude = Constants.DEFAULT_LOCATION.longitude,
                rate = rate,
                status = status,
                startDate = startDate,
                typeDriver = typeDriver,
                typeVehicle = typeVehicle,
                licensePlateNumber = licensePlateNumber
            )
            listDriverHashMap[driverInfo.driverId] = driverInfo
        }
    }
}