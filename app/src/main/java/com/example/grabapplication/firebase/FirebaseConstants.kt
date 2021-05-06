package com.example.grabapplication.firebase

class FirebaseConstants {
    companion object {
        const val KEY_DRIVERS = "drivers"
        const val KEY_USERS = "users"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_TOKEN_ID = "tokenId"

        // Firebase Cloud Message
        const val KEY_START_ADDRESS = "startAddress"
        const val KEY_END_ADDRESS = "endAddress"
        const val KEY_TO = "to"
        const val KEY_DATA = "data"
        const val KEY_AUTHORIZATION = "Authorization"
        const val KEY_CONTENT_TYPE = "Content-Type"
        const val FCM_API = "https://fcm.googleapis.com/fcm/send"
        const val SERVER_KEY = "key=AAAATeiFcyw:APA91bG8McERiKb8mFwJvV337r3U5Z46ARLINXd5tgWbmWe-P4nHB_d3V5HvzugjmHgTo87KtMWev30APVmGCaPG2Npm9eq0BExRZKU0HvqpeeSnEF2BNp7ptlq5IBHKeBa6FQ65cgaI"
        const val CONTENT_TYPE = "application/json"
    }
}