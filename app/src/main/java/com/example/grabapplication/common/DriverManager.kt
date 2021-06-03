package com.example.grabapplication.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.firebase.FirebaseManager
import com.example.grabapplication.firebase.FirebaseUtils
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.DriverInfoKey
import com.example.grabapplication.model.DriverStatus
import com.example.grabapplication.model.SexValue
import com.example.grabapplication.services.GetListDriverReceiver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.util.*
import kotlin.math.abs

class DriverManager private constructor() {
    var listDriverHashMap: LinkedHashMap<String, DriverInfo> = LinkedHashMap()
    private var listEventListener: HashMap<String, ValueEventListener> = HashMap()
    private val databaseDrivers = FirebaseManager.getInstance().databaseDrivers
    private var listenerStatusHistory : ValueEventListener? = null
    private val appPreferences: AppPreferences by lazy { AppPreferences.getInstance(GrabApplication.getAppContext()) }

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

    fun getDriverInfoFromDataSnapshot(snapshot: DataSnapshot): String {
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
        return driverId!!
    }

    fun getHistoryDriverInfoFromDataSnapshot(snapshot: DataSnapshot, driverInfo: DriverInfo) {
        driverInfo.apply {
            latitude = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyLatitude.rawValue)
            longitude = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyLongitude.rawValue)
            status = FirebaseUtils.getIntFromDataSnapshot(snapshot, DriverInfoKey.KeyStatus.rawValue)
            tokenId = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyTokenId.rawValue)
        }
    }

    fun getListDriverFromServer(callback: (Boolean) -> Unit) {
        HttpConnection.getInstance().getListDriver { isSuccess, jsonObject ->
            if (isSuccess) {
                MainActivity.clearMarkerDriver()
                clearAllEventListener()
                getListDriverFromJsonObject(JSONObject(jsonObject))
                getInfoDriver()
                callback.invoke(true)
            }
        }
        scheduleGetListDriver()
    }

    private fun getInfoDriver() {
        for (driver in listDriverHashMap) {
            val driverInfo = driver.value
            val listener = FirebaseManager.getInstance().databaseDrivers.child(driverInfo.driverId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("NamTV", "onDataChange $snapshot")
                    val driverId = getDriverInfoFromDataSnapshot(snapshot)
                    val itemDriverInfo = listDriverHashMap[driverId]
                    if (itemDriverInfo != null) {
                        if (itemDriverInfo.status != DriverStatus.StatusOn.rawValue) {
                            MainActivity.removeMarkerDriver(driverId)
                            listDriverHashMap.remove(driverId)
                        } else {
                            MainActivity.addOrUpdateMarkerDriver(listDriverHashMap[driverId]!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("NamTV", "onCancelled")
                }
            })
            listEventListener[driverInfo.driverId] = listener
            Log.d("NamTV", "getInfoDriver listener driver ${driverInfo.driverId}")
        }
    }

    private fun clearAllEventListener() {
        for (eventListener in listEventListener) {
//            databaseDrivers.removeEventListener(eventListener.value)
        }
    }

    fun getStatusHistoryBookInfo(driverInfo: DriverInfo, callback : (Boolean) -> Unit) {
        listenerStatusHistory = databaseDrivers.child(driverInfo.driverId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                getHistoryDriverInfoFromDataSnapshot(snapshot, driverInfo)
                MainActivity.addOrUpdateMarkerDriver(driverInfo)
                removeEventListenerStatusHistory()
                callback.invoke(true)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("NamTV", "onCancelled")
            }
        })
        Log.d("NamTV", "getStatusHistoryBookInfo listener driver ${driverInfo.driverId}")
    }

    private fun removeEventListenerStatusHistory() {
        databaseDrivers.removeEventListener(listenerStatusHistory!!)
    }

    private fun scheduleGetListDriver() {
        GrabApplication.getAppContext().let {
            val getListDriverReceiver = Intent(it, GetListDriverReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(it, Constants.REQUEST_GET_LIST_DRIVER, getListDriverReceiver, 0)
            val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.TIME_SCHEDULE_GET_LIST_DRIVER, pendingIntent)
        }
    }

    private fun getListDriverFromJsonObject(jsonObject: JSONObject) {
        val newListDriver = HashMap<String, DriverInfo>()

        val listDriver = CommonUtils.getJsonArrayFromJsonObject(jsonObject, FirebaseConstants.KEY_DRIVERS)
        for (i in 0 until listDriver.length()) {
            val driverJsonObject = listDriver.getJSONObject(i)
            val driverId = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyDriverId.rawValue)
            val tokenId = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyTokenId.rawValue)
            val name =  CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyName.rawValue)
            val age = CommonUtils.getIntFromJsonObject(driverJsonObject, DriverInfoKey.KeyAge.rawValue)
            val sex = CommonUtils.getIntFromJsonObject(driverJsonObject, DriverInfoKey.KeySex.rawValue)
            val sexValue = if (sex == 0) {
                SexValue.MALE.rawValue
            } else {
                SexValue.FEMALE.rawValue
            }
            val phoneNumber = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyPhoneNumber.rawValue)
            val rate = CommonUtils.getFloatFromJsonObject(driverJsonObject, DriverInfoKey.KeyRate.rawValue)
            val status = CommonUtils.getIntFromJsonObject(driverJsonObject, DriverInfoKey.KeyStatus.rawValue)
            if (status != DriverStatus.StatusOn.rawValue) {
                continue
            }
            val startDate = CommonUtils.getDateFromJsonObject(driverJsonObject, DriverInfoKey.KeyStartDate.rawValue)
            val typeDriver = CommonUtils.getTypeDriver(driverJsonObject, DriverInfoKey.KeyTypeDriver.rawValue)
            val typeVehicle = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyTypeVehicle.rawValue)
            val licensePlateNumber = CommonUtils.getStringFromJsonObject(driverJsonObject, DriverInfoKey.KeyLicensePlateNumber.rawValue)
            val distance = CommonUtils.getFloatFromJsonObject(driverJsonObject, DriverInfoKey.KeyDistance.rawValue)
            val point = scoreDriver(rate, distance, age)

            val driverInfo = DriverInfo(
                driverId = driverId,
                tokenId = tokenId,
                name = name,
                age = age,
                sex = sexValue,
                phoneNumber = phoneNumber,
                latitude = Constants.DEFAULT_LOCATION.latitude,
                longitude = Constants.DEFAULT_LOCATION.longitude,
                rate = rate,
                status = status,
                startDate = startDate,
                typeDriver = typeDriver,
                typeVehicle = typeVehicle,
                licensePlateNumber = licensePlateNumber,
                distance = distance,
                point = point
            )
            newListDriver[driverInfo.driverId] = driverInfo
        }

        if (newListDriver.isNotEmpty()) {
            sortListDriver(newListDriver)
        }
        Toast.makeText(GrabApplication.getAppContext(), "listDriverHashMap = ${listDriverHashMap.size} ", Toast.LENGTH_LONG).show()
        Log.d("NamTV", "listDriverHashMap = ${listDriverHashMap.size} $listDriver")
    }

    private fun sortListDriver(newListDriver: HashMap<String, DriverInfo>) {
        val result = newListDriver.toList().sortedBy { (_, value) -> value.distance}.toMap()
        listDriverHashMap.clear()
        for (entry in result) {
            listDriverHashMap[entry.key] = entry.value
        }
        Log.d("NamTV", "listDriverHashMap size = ${listDriverHashMap.size}")
    }

    private fun scoreDriver(rate: Float, distance: Float, age: Int): Float {
        return (rate * appPreferences.ratePoint - distance * 1000 * appPreferences.distancePoint - abs(AccountManager.getInstance().getAge() - age) * appPreferences.agePoint)
    }
}