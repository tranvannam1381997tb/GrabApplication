package com.example.grabapplication.common

import com.google.android.gms.maps.model.LatLng

class Constants {
    companion object {
        const val DEFAULT_ZOOM_MAPS = 15

        const val DATE_FORMAT_FOR_FIREBASE = "dd/MM/yyyy"
        const val DATE_FORMAT_APP = "dd, MMM yyyy"
        const val KEY_SUCCESS = "success"

        val DEFAULT_LOCATION = LatLng(-33.8523341, 151.2106085)

        const val FRAGMENT_MAP = 0
        const val FRAGMENT_INFO_DRIVER = 1
        const val FRAGMENT_FIND_PLACE = 2
        const val FRAGMENT_WAIT_DRIVER = 3
        const val FRAGMENT_DRIVER_GOING = 4
        const val FRAGMENT_BILL = 5

        const val TIME_WAIT_DRIVER: Long = 60 * 1000
        const val COUNT_DOWN_INTERVAL: Long = 1 * 1000

        const val REQUEST_GET_LIST_DRIVER = 100
        const val TIME_SCHEDULE_GET_LIST_DRIVER = 60*1000
    }
}