package com.example.grabapplication

import android.app.Application
import android.content.Context

class GrabApplication: Application() {
    companion object {
        private lateinit var context: Context
        var isActive = false
        fun getAppContext(): Context {
            return context
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}