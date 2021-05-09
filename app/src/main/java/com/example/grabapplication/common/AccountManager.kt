package com.example.grabapplication.common

import android.util.Log
import com.example.grabapplication.firebase.FirebaseManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.iid.FirebaseInstanceId

class AccountManager private constructor() {

    private var idUser: String? = null
    private var tokenId: String? = null
    private var name: String? = null
    private var age: Int? = null
    private var sex: Int? = null
    private var phoneNumber: String? = null
    private var currentLocation: LatLng? = null
    private var status: Int? = null

    companion object {
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

    fun saveIdUser(id: String) {
        idUser = id
    }

    fun getIdUser(): String{
        return idUser ?: ""
    }

    fun getTokenIdDevice(callback: (String?) -> Unit) {
        if (tokenId != null) {
            callback.invoke(tokenId)
            FirebaseManager.getInstance().updateTokenIdToFirebase(tokenId!!)
            return
        }
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if(!it.isSuccessful){
                Log.e("NamTV", "getInstanceId failed", it.exception)
                return@addOnCompleteListener
            }
            val token =  it.result?.token
            Log.d("NamTV", "$token")
            callback.invoke(token)
            FirebaseManager.getInstance().updateTokenIdToFirebase(token!!)
        }
    }

    fun setLocationUser(location: LatLng) {
        currentLocation = location
        FirebaseManager.getInstance().updateLocationUserToFirebase(location)
    }

    fun getLocationUser(): LatLng {
        return currentLocation ?: Constants.DEFAULT_LOCATION
    }

    fun setUserInfo(name: String, age: Int, sex: Int, phoneNumber: String, status: Int) {
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

    fun getSex(): Int {
        return sex!!
    }

    fun getAge(): Int {
        return age!!
    }

    fun getPhoneNumber(): String {
        return phoneNumber!!
    }
}

enum class SexValue(val rawValue: Int) {
    MALE(0),
    FEMALE(1)
}