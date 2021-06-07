package com.example.grabapplication.firebase

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.model.BookInfo
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

    fun pushNotifyToDriver(bookInfo: BookInfo, callback: (Boolean) -> Unit) {
        FirebaseMessaging.getInstance().subscribeToTopic(bookInfo.driverInfo!!.tokenId)
        val notification = createBodyRequestPush(bookInfo)
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

    private fun createBodyRequestPush(bookInfo: BookInfo): JSONObject {
        val notification = JSONObject()
        val notificationBody = JSONObject()
        val notificationData = JSONObject()

        try {
            val accountManager = AccountManager.getInstance()
            notificationData.put(FirebaseConstants.KEY_START_ADDRESS, bookInfo.startAddress)
            notificationData.put(FirebaseConstants.KEY_END_ADDRESS, bookInfo.endAddress)
            notificationData.put(FirebaseConstants.KEY_LAT_START, bookInfo.latStart)
            notificationData.put(FirebaseConstants.KEY_LNG_START, bookInfo.lngStart)
            notificationData.put(FirebaseConstants.KEY_LAT_END, bookInfo.latEnd)
            notificationData.put(FirebaseConstants.KEY_LNG_END, bookInfo.lngEnd)
            notificationData.put(FirebaseConstants.KEY_USER_ID, accountManager.getUserId())
            notificationData.put(FirebaseConstants.KEY_DRIVER_ID, bookInfo.driverInfo!!.driverId)
            notificationData.put(FirebaseConstants.KEY_PRICE, bookInfo.price)
            notificationData.put(FirebaseConstants.KEY_DISTANCE, bookInfo.distance)
            notificationData.put(FirebaseConstants.KEY_TOKEN_ID, accountManager.getTokenId())
            notificationData.put(FirebaseConstants.KEY_NAME, accountManager.getName())
            notificationData.put(FirebaseConstants.KEY_SEX, accountManager.getSex())
            notificationData.put(FirebaseConstants.KEY_AGE, accountManager.getAge())
            notificationData.put(FirebaseConstants.KEY_PHONE_NUMBER, accountManager.getPhoneNumber())

            notificationBody.put(FirebaseConstants.KEY_BOOK_DRIVER, notificationData)

            notification.put(FirebaseConstants.KEY_TO, bookInfo.driverInfo!!.tokenId)
            notification.put(FirebaseConstants.KEY_DATA, notificationBody)
            Log.d("NamTV", "notify = $notification")

        } catch (e: JSONException) {
            Log.e("NamTV", "FirebaseConnection::pushNotifyToDriver: $e")
        } finally {
            return notification
        }
    }

    fun pushNotifyToCancelBook(bookInfo: BookInfo, callback: (Boolean) -> Unit) {
        FirebaseMessaging.getInstance().subscribeToTopic(bookInfo.driverInfo!!.driverId)
        val notification = createBodyRequestCancelBook(bookInfo)
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

    private fun createBodyRequestCancelBook(bookInfo: BookInfo): JSONObject {
        val notification = JSONObject()
        val notificationBody = JSONObject()
        val notificationData = JSONObject()

        try {
            val accountManager = AccountManager.getInstance()
            notificationData.put(FirebaseConstants.KEY_DRIVER_ID, bookInfo.driverInfo!!.driverId)
            notificationData.put(FirebaseConstants.KEY_USER_ID, accountManager.getUserId())

            notificationBody.put(FirebaseConstants.KEY_CANCEL_BOOK, notificationData)

            notification.put(FirebaseConstants.KEY_TO, bookInfo.driverInfo!!.tokenId)
            notification.put(FirebaseConstants.KEY_DATA, notificationBody)
            Log.d("NamTV", "notify = $notification")

        } catch (e: JSONException) {
            Log.e("NamTV", "FirebaseConnection::pushNotifyToDriver: $e")
        } finally {
            return notification
        }
    }
}