package com.example.grabapplication.googlemaps

class MapsConstant {
    companion object {
        const val URL_DIRECTION = "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&key=%s"
        const val URL_FIND_PLACE = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s"
        const val URL_ENCODE_CHARSETS = "UTF-8"
        const val BASE_URL_GOOGLE_MAP = "https://maps.googleapis.com"

        // google directions api key
        const val DIRECTION_ROUTES = "routes"
        const val DIRECTION_LEGS = "legs"
        const val DIRECTION_STEPS = "steps"
        const val DIRECTION_POLYLINE = "polyline"
        const val DIRECTION_POINTS = "points"

        // google geocoding api key
        const val GEOCODING_RESULTS = "results"
        const val GEOCODING_GEOMETRY = "geometry"
        const val GEOCODING_LOCATION = "geometry"
        const val GEOCODING_LAT = "lat"
        const val GEOCODING_LNG = "lng"
        const val GEOCODING_FORMATTED_ADDRESS = "formatted_address"
    }
}