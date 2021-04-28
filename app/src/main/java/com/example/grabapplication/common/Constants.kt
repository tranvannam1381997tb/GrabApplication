package com.example.grabapplication.common

import com.google.android.gms.maps.model.LatLng

class Constants {
    companion object {
        const val DEFAULT_ZOOM_MAPS = 15

        const val DATE_FORMAT_FOR_FIREBASE = "dd/MM/yyyy"
        const val DATE_FORMAT_APP = "dd, MMM yyyy"

        val DEFAULT_LOCATION = LatLng(-33.8523341, 151.2106085)
    }
}