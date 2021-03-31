package com.example.grabapplication.connecttion

import android.util.Log
import com.example.grabapplication.googlemaps.MapsConnection
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class HttpConnection private constructor() {

    fun startURLConnection(startAddress : String, endAddress : String, distance: Int) {
        val url = URL("http://192.168.1.105:3000/hello-post")
        val http = url.openConnection() as HttpURLConnection
        http.connectTimeout = 30000
        http.requestMethod = "POST"

        // set body data
//        val body = StringBuilder()
//        body.append("{\n")
//        body.append("\"startAddress\" : \"$startAddress\",\n")
//        body.append("\"endAddress\" : \"$endAddress\",\n")
//        body.append("\"distance\" : \"$distance\"\n")
//        body.append("}")

        val jsonBody = JSONObject()
        jsonBody.put("startAddress", startAddress)
        jsonBody.put("endAddress", endAddress)
        jsonBody.put("distance", distance)

        val outputInBytes = jsonBody.toString().toByteArray(Charsets.UTF_8)
        val os = http.outputStream
        os.write(outputInBytes)
        os.close()

        requestHttps(http)
    }

    private fun requestHttps(http: HttpURLConnection) {
        var inputStream: InputStream? = null
        var inputStreamReader: InputStreamReader? = null
        var buffer: BufferedReader? = null
        try {
            http.connect()
            val responseCode = http.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    inputStream = http.inputStream
                    inputStreamReader = InputStreamReader(inputStream)
                    buffer = BufferedReader(inputStreamReader)
                    val data = getByteArrayFromStream(buffer)
                    Log.d("NamTV", "connect success: $data")
                }

                else -> {
                    inputStream = http.errorStream
                    inputStreamReader = InputStreamReader(inputStream)
                    buffer = BufferedReader(inputStreamReader)
                    val error = getByteArrayFromStream(buffer)
                    Log.d("NamTV", "connect error: $error")
                }
            }
        } catch (e: Exception) {
            Log.d("NamTV", "startURLConnection::exception: $e")
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