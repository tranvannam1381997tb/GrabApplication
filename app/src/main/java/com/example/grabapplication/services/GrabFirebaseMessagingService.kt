package com.example.grabapplication.services

import android.content.Intent
import android.util.Log
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.firebase.FirebaseManager
import com.google.firebase.messaging.FirebaseMessagingService
import org.json.JSONException
import org.json.JSONObject

class GrabFirebaseMessagingService : FirebaseMessagingService() {

    override fun handleIntent(intent: Intent) {
        val action = intent.action
        if ("com.google.android.c2dm.intent.RECEIVE" == action || "com.google.firebase.messaging.RECEIVE_DIRECT_BOOT" == action) {
            Log.d("NamTV", "handleIntent")

            if (intent.hasExtra(FirebaseConstants.KEY_DRIVER_RESPONSE)) {
                try {
                    val jsonData = JSONObject(intent.getStringExtra(FirebaseConstants.KEY_DRIVER_RESPONSE)!!)
                    Log.d("NamTV", "jsonData = $jsonData")
                    if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_GOING_BOOK)) {
                        // Driver going to pick you up
                        bookListener?.handleDriverGoingBook(jsonData)
                    } else if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_REJECT)) {
                        // Driver reject book
                        bookListener?.handleDriverReject()
                    } else if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_ARRIVED_ORIGIN)) {
                        // Driver arrived
                        bookListener?.handleDriverArrivedOrigin(jsonData)
                    } else if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_GOING)) {
                        bookListener?.handleDriverGoing(jsonData)
                    } else if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_ARRIVED_DESTINATION)) {
                        bookListener?.handleDriverArrivedDestination(jsonData)
                    } else if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_BILL)) {
                        bookListener?.handleDriverBill()
                    }
                } catch (e: JSONException) {
                    // Do nothing
                }
            }
        } else {
            super.handleIntent(intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NamTV", "New Device Token $token")
        FirebaseManager.getInstance().updateTokenIdToFirebase(token)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

    }

    companion object {
        var bookListener: BookListener? = null
    }
}

interface BookListener {
    fun handleDriverGoingBook(jsonData: JSONObject)

    fun handleDriverReject()

    fun handleDriverArrivedOrigin(jsonData: JSONObject)

    fun handleDriverGoing(jsonData: JSONObject)

    fun handleDriverArrivedDestination(jsonData: JSONObject)

    fun handleDriverBill()
}