package com.example.grabapplication.connecttion

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class HttpConnection private constructor() {

    fun startLogin(username: String, password: String): CompletionHandler {
        try {
            val url = URL(String.format(URL_LOGIN_FORMAT, HOST))
            val http = url.openConnection() as HttpURLConnection
            http.connectTimeout = CONNECTION_TIMEOUT
            http.requestMethod = POST

            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            http.setRequestProperty("Accept", "application/json")

            val jsonBody = JSONObject()
            jsonBody.put("username", username)
            jsonBody.put("password", password)

            val outputInBytes = jsonBody.toString().toByteArray(Charsets.UTF_8)
            val os = http.outputStream
            os.write(outputInBytes)
            os.close()

            return requestHttps(http)
        } catch (e: MalformedURLException) {
            Log.d(" NamTV","HttpConnection::startLogin::MalformedURLException: $e")
            return CompletionHandler(null, e.toString(), 1)
        } catch (e: Exception) {
            Log.d("NamTV", "HttpConnection::startLogin:exception: $e")
            return CompletionHandler(null, e.toString(), 1)
        }
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
        private const val URL_LOGIN_FORMAT = "http://%s/login/user"
        private const val HOST = "192.168.1.105:3000"
        private const val POST = "POST"
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
