package com.example.grabapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.grabapplication.manager.AccountManager
import java.util.*

class GetPolicyReceiver  : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            AccountManager.getInstance().getPolicy()
            Log.d("NamTV", "GetPolicyReceiver::schedule update policy ${Calendar.getInstance().time}")
        }
    }
}