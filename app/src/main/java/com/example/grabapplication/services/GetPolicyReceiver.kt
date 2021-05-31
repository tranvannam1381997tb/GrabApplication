package com.example.grabapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.common.DriverManager
import com.example.grabapplication.connecttion.HttpConnection
import java.util.*

class GetPolicyReceiver  : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            AccountManager.getInstance().getPolicy()
            Log.d("NamTV", "GetPolicyReceiver::schedule update policy ${Calendar.getInstance().time}")
        }
    }
}