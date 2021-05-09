package com.example.grabapplication.firebase

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.googlemaps.models.Distance
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject

class FirebaseConnection private constructor() {

    companion object {
        private var instance: FirebaseConnection? = null
        fun getInstance(): FirebaseConnection {
            if (instance == null) {
                synchronized(FirebaseConnection::class.java) {
                    if (instance == null) {
                        instance = FirebaseConnection()
                    }
                }
            }
            return instance!!
        }
    }

    fun pushNotifyToDriver(distancePlaceChoose: Distance, idDriver: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(idDriver)
        val notification = createBodyRequestPush(distancePlaceChoose, idDriver)
        val jsonObjectRequest = object : JsonObjectRequest(FirebaseConstants.FCM_API, notification,
            Response.Listener<JSONObject> {
                Log.d("NamTV", "JsonObjectRequest Response.Listener + $it")
            }, Response.ErrorListener {

                Log.d("NamTV", "JsonObjectRequest Response.ErrorListener + $it")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params[FirebaseConstants.KEY_AUTHORIZATION] = FirebaseConstants.SERVER_KEY
                params[FirebaseConstants.KEY_CONTENT_TYPE] = FirebaseConstants.CONTENT_TYPE
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    private fun createBodyRequestPush(distancePlaceChoose: Distance, idDriver: String): JSONObject {
        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            val accountManager = AccountManager.getInstance()
            notificationBody.put(FirebaseConstants.KEY_START_ADDRESS, distancePlaceChoose.startAddress)
            notificationBody.put(FirebaseConstants.KEY_END_ADDRESS, distancePlaceChoose.endAddress)
            notificationBody.put(FirebaseConstants.KEY_USER_ID, accountManager.getIdUser())
            notificationBody.put(FirebaseConstants.KEY_PRICE, "10000")
            notificationBody.put(FirebaseConstants.KEY_DISTANCE, distancePlaceChoose.distanceText)
            notificationBody.put(FirebaseConstants.KEY_TOKEN_ID, accountManager.getTokenId())
            Log.d("NamTV", "token = ${accountManager.getTokenId()}")
            notificationBody.put(FirebaseConstants.KEY_NAME, accountManager.getName())
            notificationBody.put(FirebaseConstants.KEY_SEX, accountManager.getSex())
            notificationBody.put(FirebaseConstants.KEY_AGE, accountManager.getAge())
            notificationBody.put(FirebaseConstants.KEY_PHONE_NUMBER, accountManager.getPhoneNumber())
            notification.put(FirebaseConstants.KEY_TO, idDriver)
            notification.put(FirebaseConstants.KEY_DATA, notificationBody)
            Log.d("NamTV", "notify = $notificationBody")

        } catch (e: JSONException) {
            Log.e("NamTV", "FirebaseConnection::pushNotifyToDriver: $e")
        } finally {
            return notification
        }
    }
}