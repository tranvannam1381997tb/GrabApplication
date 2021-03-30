package com.example.grabapplication.connecttion

import com.example.grabapplication.googlemaps.MapsConnection

class HttpConnection private constructor() {


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