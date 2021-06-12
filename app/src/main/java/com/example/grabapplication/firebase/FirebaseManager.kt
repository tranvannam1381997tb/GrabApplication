package com.example.grabapplication.firebase

import com.example.grabapplication.manager.AccountManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseManager private constructor() {

    companion object {
        private var instance: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            if (instance == null) {
                synchronized(FirebaseManager::class.java) {
                    if (instance == null) {
                        instance = FirebaseManager()
                    }
                }
            }
            return instance!!
        }
    }

    val databaseDrivers: DatabaseReference
            by lazy {
                FirebaseDatabase.getInstance().reference.child(FirebaseConstants.KEY_DRIVERS)
            }

    private val databaseUsers: DatabaseReference
            by lazy {
                FirebaseDatabase.getInstance().reference.child(FirebaseConstants.KEY_USERS)
            }

    fun updateLocationUserToFirebase(location: LatLng) {
        val idUser = AccountManager.getInstance().getUserId()
        if (idUser.isNotEmpty()) {
            databaseUsers.child(idUser).child(FirebaseConstants.KEY_LATITUDE).setValue(location.latitude)
            databaseUsers.child(idUser).child(FirebaseConstants.KEY_LONGITUDE).setValue(location.longitude)
        }
    }

    fun updateTokenIdToFirebase(tokenId: String) {
        val idUser = AccountManager.getInstance().getUserId()
        if (idUser.isNotEmpty()) {
            databaseUsers.child(idUser).child(FirebaseConstants.KEY_TOKEN_ID).setValue(tokenId)
        }
    }
}