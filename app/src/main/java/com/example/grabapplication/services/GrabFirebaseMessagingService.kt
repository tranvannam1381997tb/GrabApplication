package com.example.grabapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.common.Constants
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.firebase.FirebaseManager
import com.google.firebase.messaging.FirebaseMessagingService
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class GrabFirebaseMessagingService : FirebaseMessagingService() {

    private var notificationChannelId: String = "Grab Application"
    private val mNotificationManager = GrabApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as
            NotificationManager

    override fun handleIntent(intent: Intent) {
        val action = intent.action
        if ("com.google.android.c2dm.intent.RECEIVE" == action || "com.google.firebase.messaging.RECEIVE_DIRECT_BOOT" == action) {
            Log.d("NamTV", "handleIntent")

            if (intent.hasExtra(FirebaseConstants.KEY_DRIVER_RESPONSE)) {
                try {
                    val jsonData = JSONObject(intent.getStringExtra(FirebaseConstants.KEY_DRIVER_RESPONSE)!!)
                    Log.d("NamTV", "jsonData = $jsonData")
                    var message = ""
                    when {
                        CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_GOING_BOOK) -> {
                            // Driver going to pick you up
                            bookListener?.handleDriverGoingBook(jsonData)
                            val timeArrivedOrigin = jsonData.getInt(FirebaseConstants.KEY_TIME_ARRIVED_ORIGIN)
                            val currentTime = Calendar.getInstance()
                            currentTime.add(Calendar.SECOND, timeArrivedOrigin)
                            val timeDriverArrived = CommonUtils.getTimeArrived(currentTime)
                            message = getString(R.string.notify_time_driver_arrived_origin, timeDriverArrived)
                        }
                        CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_REJECT) -> {
                            // Driver reject book
                            bookListener?.handleDriverReject()
                        }
                        CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_ARRIVED_ORIGIN) -> {
                            // Driver arrived
                            bookListener?.handleDriverArrivedOrigin(jsonData)
                            message = getString(R.string.driver_arrived_origin)
                        }
                        CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_GOING) -> {
                            bookListener?.handleDriverGoing(jsonData)
                            message = getString(R.string.notify_driver_start_arriving_destination)
                        }
                        CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_ARRIVED_DESTINATION) -> {
                            bookListener?.handleDriverArrivedDestination(jsonData)
                            message = getString(R.string.notify_driver_arrived_destination)
                        }
                        CommonUtils.getBooleanFromJsonObject(jsonData, FirebaseConstants.KEY_DRIVER_BILL) -> {
                            bookListener?.handleDriverBill()
                            message = getString(R.string.notify_bill)
                        }
                    }
                    showNotification(GrabApplication.getAppContext(), message)
                } catch (e: JSONException) {
                    // Do nothing
                }
            }
        } else {
            super.handleIntent(intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NamTV", "New Device Token $token")
        FirebaseManager.getInstance().updateTokenIdToFirebase(token)
    }

    private fun showNotification(context: Context, message: String) {
        if (message.isEmpty()) {
            return
        }

        val channel = NotificationChannel(
                notificationChannelId,
                notificationChannelId,
                NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = notificationChannelId
        mNotificationManager.createNotificationChannel(channel)

        val notifyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.NOTIFICATION_CONTENT, Constants.NOTIFICATION_CONTENT)
        }
        val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mBuilder = NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(R.drawable.notification) // notification icon
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))// message for notification
                .setContentIntent(pendingIntent)



        mNotificationManager.cancel(0)
        if (message == getString(R.string.notify_bill)) {
            mBuilder.setAutoCancel(true)
            mNotificationManager.notify(1, mBuilder.build())
        } else {
            mNotificationManager.notify(0, mBuilder.build())
        }
    }

    companion object {
        var bookListener: BookListener? = null
    }
}

interface BookListener {
    fun handleDriverGoingBook(jsonData: JSONObject)

    fun handleDriverReject()

    fun handleDriverArrivedOrigin(jsonData: JSONObject)

    fun handleDriverGoing(jsonData: JSONObject)

    fun handleDriverArrivedDestination(jsonData: JSONObject)

    fun handleDriverBill()
}