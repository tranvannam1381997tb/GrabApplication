package com.example.grabapplication

import android.app.Application
import android.content.Context

class GrapApplication: Application() {
    companion object {
        private lateinit var context: Context
        fun getAppContext(): Context {
            return context
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}