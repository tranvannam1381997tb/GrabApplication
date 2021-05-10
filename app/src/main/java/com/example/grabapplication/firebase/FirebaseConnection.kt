package com.example.grabapplication.firebase

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.common.Constants
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.googlemaps.models.Distance
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import javax.security.auth.callback.Callback

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

    fun pushNotifyToDriver(distancePlaceChoose: Distance, idDriver: String, callback: (Boolean) -> Unit) {
        FirebaseMessaging.getInstance().subscribeToTopic(idDriver)
        val notification = createBodyRequestPush(distancePlaceChoose, idDriver)
        val jsonObjectRequest = object : JsonObjectRequest(FirebaseConstants.FCM_API, notification,
            Response.Listener<JSONObject> {
                Log.d("NamTV", "JsonObjectRequest Response.Listener + $it")
                if (it.has(FirebaseConstants.KEY_SUCCESS) && it.getInt(FirebaseConstants.KEY_SUCCESS) == 1) {
                    callback.invoke(true)
                } else {
                    callback.invoke(false)
                }

            }, Response.ErrorListener {
                callback.invoke(false)
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
        val notificationData = JSONObject()

        try {
            val accountManager = AccountManager.getInstance()
            notificationData.put(FirebaseConstants.KEY_START_ADDRESS, distancePlaceChoose.startAddress)
            notificationData.put(FirebaseConstants.KEY_END_ADDRESS, distancePlaceChoose.endAddress)
            notificationData.put(FirebaseConstants.KEY_USER_ID, accountManager.getIdUser())
            notificationData.put(FirebaseConstants.KEY_PRICE, "10000")
            notificationData.put(FirebaseConstants.KEY_DISTANCE, distancePlaceChoose.distanceText)
            notificationData.put(FirebaseConstants.KEY_TOKEN_ID, accountManager.getTokenId())
            notificationData.put(FirebaseConstants.KEY_NAME, accountManager.getName())
            notificationData.put(FirebaseConstants.KEY_SEX, accountManager.getSex())
            notificationData.put(FirebaseConstants.KEY_AGE, accountManager.getAge())
            notificationData.put(FirebaseConstants.KEY_PHONE_NUMBER, accountManager.getPhoneNumber())

            notificationBody.put(FirebaseConstants.KEY_BOOK_DRIVER,notificationData)

            notification.put(FirebaseConstants.KEY_TO, idDriver)
            notification.put(FirebaseConstants.KEY_DATA, notificationBody)
            Log.d("NamTV", "notify = $notification")

        } catch (e: JSONException) {
            Log.e("NamTV", "FirebaseConnection::pushNotifyToDriver: $e")
        } finally {
            return notification
        }
    }
}