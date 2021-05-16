package com.example.grabapplication.connecttion

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.common.Constants
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.UserInfoKey
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

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

    fun startGetPrice(startAddress : String, endAddress : String, distance: Int): CompletionHandler {
        try {
            val url = URL("http://192.168.1.105:3000/get-price")
            val http = url.openConnection() as HttpURLConnection
            http.connectTimeout = 30000
            http.requestMethod = "POST"

            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            http.setRequestProperty("Accept", "application/json")

            val jsonBody = JSONObject()
            jsonBody.put("startAddress", startAddress)
            jsonBody.put("endAddress", endAddress)
            jsonBody.put("distance", distance)

            val outputInBytes = jsonBody.toString().toByteArray(Charsets.UTF_8)
            val os = http.outputStream
            os.write(outputInBytes)
            os.close()

            return requestHttps(http)
        } catch (e: MalformedURLException) {
            Log.d("NamTV", "HttpConnection::startURLConnection: MalformedURLException: $e")
            return CompletionHandler(null, e.toString(), 0)
        } catch (e: Exception) {
            Log.d("NamTV", "HttpConnection::startURLConnection: Exception: $e")
            return CompletionHandler(null, e.toString(), 0)
        }
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

    private fun requestHttps(http: HttpURLConnection): CompletionHandler {
        var inputStream: InputStream? = null
        var inputStreamReader: InputStreamReader? = null
        var buffer: BufferedReader? = null
        try {
            http.connect()
            val responseCode = http.responseCode
            val completionHandler = CompletionHandler(null, null, responseCode)
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    inputStream = http.inputStream
                    inputStreamReader = InputStreamReader(inputStream)
                    buffer = BufferedReader(inputStreamReader)
                    val data = getByteArrayFromStream(buffer)
                    Log.d("NamTV", "connect success: $data")
                    completionHandler.data = data
                }

                else -> {
                    inputStream = http.errorStream
                    inputStreamReader = InputStreamReader(inputStream)
                    buffer = BufferedReader(inputStreamReader)
                    val error = getByteArrayFromStream(buffer)
                    Log.d("NamTV", "connect error: $error")
                    completionHandler.error = error
                }
            }
            return completionHandler
        } catch (e: Exception) {
            Log.d("NamTV", "startURLConnection::exception: $e")
            return CompletionHandler(null, e.toString(), 1)
        }
    }

    private fun getByteArrayFromStream(buffer: BufferedReader?): String {
        val data = StringBuilder()
        var line: String?
        line = buffer!!.readLine()
        while (line != null) {
            data.append(line)
            data.append("\n")
            line = buffer.readLine()
        }
        return data.toString()
    }

    companion object {
        private const val URL_LOGIN_FORMAT = "http://%s/api/user/login"
        private const val URL_SIGN_UP = "http://%s/api/user/create"
        private const val URL_GET_LIST_DRIVER = "http://%s/api/user/find-drivers"
        private const val HOST = "192.168.1.215:3000"
        private const val CONNECTION_TIMEOUT = 30000

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

class CompletionHandler(var data: String?, var error: String?, var responseCode: Int?)
