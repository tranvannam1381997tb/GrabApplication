package com.example.grabapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.grabapplication.common.DriverManager

class GetListDriverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            DriverManager.getInstance().getListDriverFromServer {}
            Log.d("NamTV", "GetListDriverReceiver::schedule update list driver")
        }
    }
}