package com.example.grabapplication.connecttion

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.manager.AccountManager
import com.example.grabapplication.common.Constants
import com.example.grabapplication.model.DriverInfoKey
import com.example.grabapplication.model.UserInfoKey
import org.json.JSONException
import org.json.JSONObject

class HttpConnection private constructor() {

    fun startLogin(jsonBody: JSONObject, callback:(Boolean, String) -> Unit) {
        val url = String.format(URL_LOGIN_FORMAT, HOST)
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonBody, Response.Listener<JSONObject> {
            if (it != null && it.has(Constants.KEY_SUCCESS)) {
                callback.invoke(true, it.toString())
            }
        }, Response.ErrorListener {
            try {
                if (it.networkResponse != null) {
                    val statusCode = it.networkResponse.statusCode
                    if (statusCode == 400) {
                        val dataError = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                        val error = dataError.getString("error")
                        callback.invoke(false, error)
                    }
                }
            } catch (e: Exception) {
                Log.d("NamTV", "HttpConnection::startLogin: exception = $e")
            }

            callback.invoke(false, GrabApplication.getAppContext().getString(R.string.connect_server_error))
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=utf-8"
                params["Accept"] = "application/json"
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                CONNECTION_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    fun startSignUp(jsonBody: JSONObject, callback:(Boolean, String) -> Unit) {
        val url = String.format(URL_SIGN_UP, HOST)
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonBody, Response.Listener<JSONObject> {
            if (it != null && it.has(Constants.KEY_SUCCESS)) {
                callback.invoke(true, it.toString())
            }
        }, Response.ErrorListener {
            if (it.networkResponse != null) {
                val statusCode = it.networkResponse.statusCode
                if (statusCode == 400) {
                    val dataError = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                    val error = dataError.getString("error")
                    callback.invoke(false, error)
                }
            }

            callback.invoke(false, GrabApplication.getAppContext().getString(R.string.connect_server_error))
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=utf-8"
                params["Accept"] = "application/json"
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                CONNECTION_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    fun getListDriver(callback:(Boolean, String) -> Unit) {
        val url = String.format(URL_GET_LIST_DRIVER, HOST)
        val jsonBody = JSONObject()
        jsonBody.put(UserInfoKey.KeyUserId.rawValue, AccountManager.getInstance().getUserId())
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonBody, Response.Listener<JSONObject> {
            if (it != null && it.has(Constants.KEY_SUCCESS)) {
                callback.invoke(true, it.toString())
            }
        }, Response.ErrorListener {
            if (it.networkResponse != null) {
                val statusCode = it.networkResponse.statusCode
                if (statusCode == 400) {
                    val dataError = JSONObject(it.networkResponse.data.toString(Charsets.UTF_8))
                    val error = dataError.getString("error")
                    callback.invoke(false, error)
                }
            }

            callback.invoke(false, GrabApplication.getAppContext().getString(R.string.connect_server_error))
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=utf-8"
                params["Accept"] = "application/json"
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            CONNECTION_TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    fun voteStarDriver(driverId: String, rating: Int) {
        val url = String.format(URL_RATING, HOST)
        val jsonBody = createBodyRequestVote(driverId, rating)
        Log.d("NamTV", "voteStarDriver $jsonBody")
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonBody, Response.Listener<JSONObject> {
            Log.d("NamTV", "voteStarDriver success $it")
        }, Response.ErrorListener {
            Log.d("NamTV", "voteStarDriver error $it")
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=utf-8"
                params["Accept"] = "application/json"
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            CONNECTION_TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    private fun createBodyRequestVote(driverId: String, rating: Int): JSONObject {
        val jsonObject = JSONObject()

        try {
            jsonObject.put(DriverInfoKey.KeyDriverId.rawValue, driverId)
            jsonObject.put(KEY_VOTE, rating)

            Log.d("NamTV", "json = $jsonObject")
        } catch (e: JSONException) {
            Log.e("NamTV", "FirebaseConnection::pushNotifyToDriver: $e")
        } finally {
            return jsonObject
        }
    }

    fun getPolicy(callback: (Boolean, JSONObject) -> Unit) {
        val url = String.format(URL_GET_POLICY, HOST)
        val jsonBody = JSONObject()
        jsonBody.put(DriverInfoKey.KeyDriverId.rawValue, AccountManager.getInstance().getUserId())
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, url, jsonBody, Response.Listener<JSONObject> {
            if (it.has(Constants.KEY_SUCCESS)) {
                callback.invoke(true, it)
            } else {
                callback.invoke(false, it)
            }
            Log.d("NamTV", "policy = $it")
        }, Response.ErrorListener {
            callback.invoke(false, JSONObject())
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=utf-8"
                params["Accept"] = "application/json"
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                CONNECTION_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    fun logout(callback:(Boolean) -> Unit) {
        val url = String.format(URL_LOGOUT, HOST)
        val jsonBody = JSONObject()
        jsonBody.put(UserInfoKey.KeyUserId.rawValue, AccountManager.getInstance().getUserId())
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonBody, Response.Listener<JSONObject> {
            Log.d("NamTV", "logout $it + $jsonBody")
            callback.invoke(true)
        }, Response.ErrorListener {
            Log.d("NamTV", "logout ErrorListener $it")
            callback.invoke(false)
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=utf-8"
                params["Accept"] = "application/json"
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                CONNECTION_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(jsonObjectRequest)
    }

    companion object {
        private const val URL_LOGIN_FORMAT = "http://%s/api/user/login"
        private const val URL_SIGN_UP = "http://%s/api/user/create"
        private const val URL_GET_LIST_DRIVER = "http://%s/api/user/find-drivers"
        private const val URL_RATING = "http://%s/api/user/rating"
        private const val URL_LOGOUT = "http://%s/api/user/logout"
        private const val URL_GET_POLICY = "http://%s/api/policy/get"
        private const val HOST = "52.197.102.147:3000"
        private const val CONNECTION_TIMEOUT = 30000

        private const val KEY_VOTE = "vote"

        private var instance: HttpConnection? = null
        fun getInstance(): HttpConnection {
            if (instance == null) {
                synchronized(HttpConnection::class.java) {
                    if (instance == null) {
                        instance = HttpConnection()
                    }
                }
            }
            return instance!!
        }
    }
}
