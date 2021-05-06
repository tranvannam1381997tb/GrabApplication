package com.example.grabapplication.common

import com.example.grabapplication.firebase.FirebaseUtils
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.DriverInfoKey
import com.google.firebase.database.DataSnapshot

class DriverManager private constructor() {
    var listIdDriver: ArrayList<String> = ArrayList()
    private var listDriverHasMap: HashMap<String, DriverInfo> = HashMap()

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

    fun getListDriver(): HashMap<String, DriverInfo> {
        return listDriverHasMap
    }

    fun addListIdDriver() {
        listIdDriver.add("idDriver_1")
        listIdDriver.add("idDriver_2")
    }

    fun getInfoDriverFromDataSnapshot(snapshot: DataSnapshot): DriverInfo {
        return DriverInfo(
            idDriver = snapshot.key.toString(),
            tokenId = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyTokenId.rawValue),
            name = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyName.rawValue),
            age = FirebaseUtils.getIntFromDataSnapshot(snapshot, DriverInfoKey.KeyAge.rawValue),
            sex = FirebaseUtils.getIntFromDataSnapshot(snapshot, DriverInfoKey.KeySex.rawValue),
            phoneNumber = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyPhoneNumber.rawValue),
            latitude = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyLatitude.rawValue),
            longitude = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyLongitude.rawValue),
            rate = FirebaseUtils.getDoubleFromDataSnapshot(snapshot, DriverInfoKey.KeyRate.rawValue),
            status = FirebaseUtils.getIntFromDataSnapshot(snapshot, DriverInfoKey.KeyStatus.rawValue),
            startDate = FirebaseUtils.getDateFromDataSnapshot(snapshot, DriverInfoKey.KeyStartDate.rawValue),
            typeDriver = FirebaseUtils.getTypeDriverFromDataSnapshot(snapshot, DriverInfoKey.KeyTypeDriver.rawValue),
            typeVehicle = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyTypeVehicle.rawValue),
            licensePlateNumber = FirebaseUtils.getStringFromDataSnapshot(snapshot, DriverInfoKey.KeyLicensePlateNumber.rawValue)
        )
    }

    fun getInfoDriver() {

    }
}