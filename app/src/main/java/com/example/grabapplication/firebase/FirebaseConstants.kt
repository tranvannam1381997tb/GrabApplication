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
        const val KEY_LAT_START = "latStart"
        const val KEY_LNG_START = "lngStart"
        const val KEY_LAT_END = "latEnd"
        const val KEY_LNG_END = "lngEnd"
        const val KEY_USER_ID = "userId"
        const val KEY_DRIVER_ID = "driverId"
        const val KEY_PRICE = "price"
        const val KEY_DISTANCE = "distance"
        const val KEY_NAME = "name"
        const val KEY_SEX = "sex"
        const val KEY_AGE = "age"
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_BOOK_DRIVER = "bookDriver"
        const val KEY_CANCEL_BOOK = "cancelBook"

        const val KEY_TO = "to"
        const val KEY_DATA = "data"
        const val KEY_SUCCESS = "success"
        const val KEY_AUTHORIZATION = "Authorization"
        const val KEY_CONTENT_TYPE = "Content-Type"
        const val FCM_API = "https://fcm.googleapis.com/fcm/send"
        const val SERVER_KEY = "key=AAAATeiFcyw:APA91bG8McERiKb8mFwJvV337r3U5Z46ARLINXd5tgWbmWe-P4nHB_d3V5HvzugjmHgTo87KtMWev30APVmGCaPG2Npm9eq0BExRZKU0HvqpeeSnEF2BNp7ptlq5IBHKeBa6FQ65cgaI"
        const val CONTENT_TYPE = "application/json"

        // Key driver response
        const val KEY_DRIVER_RESPONSE = "driverResponse"
        const val KEY_DRIVER_GOING_BOOK = "driverGoingBook"
        const val KEY_DRIVER_REJECT = "driverReject"
        const val KEY_DRIVER_ARRIVED_ORIGIN = "driverArrivedOrigin"
        const val KEY_DRIVER_GOING = "driverGoing"
        const val KEY_DRIVER_ARRIVED_DESTINATION = "driverArrivedDestination"
        const val KEY_DRIVER_BILL = "driverBill"

        const val KEY_TIME_ARRIVED_ORIGIN = "timeArrivedOrigin"
        const val KEY_TIME_ARRIVED_DESTINATION = "timeArrivedDestination"
    }
}