package com.example.grabapplication.services

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class GrabFirebaseMessagingService : FirebaseMessagingService() {

    override fun handleIntent(intent: Intent?) {
        val action = intent?.action
        if("com.google.android.c2dm.intent.RECEIVE" == action || "com.google.firebase.messaging.RECEIVE_DIRECT_BOOT" == action) {
            Log.d("NamTV", "handleIntent")
        } else {
            super.handleIntent(intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NamTV", "New Device Token $token")
    }
}