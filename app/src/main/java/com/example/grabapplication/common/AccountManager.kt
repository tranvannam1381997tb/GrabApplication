package com.example.grabapplication.common

import android.util.Log
import com.example.grabapplication.firebase.FirebaseManager
import com.example.grabapplication.firebase.FirebaseUtils
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
        getTokenId {
            tokenId = it
        }
    }

    fun saveIdUser(id: String) {
        idUser = id
    }

    fun getIdUser(): String{
        return idUser ?: ""
    }

    private fun getTokenId(callback: (String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if(!it.isSuccessful){
                Log.e("NamTV", "getInstanceId failed", it.exception)
                return@addOnCompleteListener
            }
            val token =  it.result?.token
            callback.invoke(token)
        }
    }

    fun setLocationUser(location: LatLng) {
        currentLocation = location
        FirebaseManager.getInstance().updateLocationUserToFirebase(location)
    }

    fun getLocationUser(): LatLng {
        return currentLocation ?: Constants.DEFAULT_LOCATION
    }
}