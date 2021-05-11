package com.example.grabapplication.services

import android.content.Intent
import android.util.Log
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.firebase.FirebaseUtils
import com.google.firebase.messaging.FirebaseMessagingService
import org.json.JSONException
import org.json.JSONObject

class GrabFirebaseMessagingService : FirebaseMessagingService() {

    override fun handleIntent(intent: Intent?) {
        val action = intent?.action
        if ("com.google.android.c2dm.intent.RECEIVE" == action || "com.google.firebase.messaging.RECEIVE_DIRECT_BOOT" == action) {
            Log.d("NamTV", "handleIntent")

            if (intent.hasExtra(FirebaseConstants.KEY_DRIVER_RESPONSE)) {
                try {
                    val jsonData = JSONObject(intent.getStringExtra(FirebaseConstants.KEY_DRIVER_RESPONSE)!!)
                    if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_GOING)) {
                        // TODO
                        bookListener?.handleDriverGoing()
                    } else if (CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_REJECT)) {
                        bookListener?.handleDriverReject()
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
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

    }

    companion object {
        var bookListener: BookListener? = null
    }
}

interface BookListener {
    fun handleDriverGoing()

    fun handleDriverReject()
}