package com.example.grabapplication.googlemaps

import com.example.grabapplication.googlemaps.models.Distance

class MapsConstant {
    companion object {
        const val URL_DIRECTION = "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&key=%s"
        const val URL_FIND_PLACE = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&key=%s"
        const val URL_ENCODE_CHARSETS = "UTF-8"
        const val BASE_URL_GOOGLE_MAP = "https://maps.googleapis.com"
        const val STATUS_OK = "OK"

        // google directions api key
        const val DIRECTION_STATUS = "status"
        const val DIRECTION_ROUTES = "routes"
        const val DIRECTION_LEGS = "legs"
        const val DIRECTION_DISTANCE = "distance"
        const val DIRECTION_DURATION = "duration"
        const val DIRECTION_TEXT = "text"
        const val DIRECTION_VALUE = "value"

        // google geocoding api key
        const val GEOCODING_RESULTS = "results"

        // google place api key
        const val PLACE_STATUS = "status"
        const val PLACE_RESULTS = "results"
        const val PLACE_GEOMETRY = "geometry"
        const val PLACE_LOCATION = "location"
        const val PLACE_LAT = "lat"
        const val PLACE_LNG = "lng"
        const val PLACE_FORMATTED_ADDRESS = "formatted_address"
        const val PLACE_NAME = "name"

        val DEFAULT_DISTANCE = Distance("1km", 1000, "10 phút", 600)
    }
}