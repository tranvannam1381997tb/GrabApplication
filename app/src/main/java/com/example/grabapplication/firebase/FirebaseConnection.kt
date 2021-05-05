package com.example.grabapplication.firebase

import com.example.grabapplication.connecttion.HttpConnection

class FirebaseConnection private constructor() {

    companion object {
        private const val URL_LOGIN_FORMAT = "http://%s/login/driver"
        private const val HOST = "192.168.1.105:3000"
        private const val POST = "POST"
        private const val CONNECTION_TIMEOUT = 30000

        private var instance: FirebaseConnection? = null
        fun getInstance(): FirebaseConnection {
            if (instance == null) {
                synchronized(HttpConnection::class.java) {
                    if (instance == null) {
                        instance = FirebaseConnection()
                    }
                }
            }
            return instance!!
        }
    }
}