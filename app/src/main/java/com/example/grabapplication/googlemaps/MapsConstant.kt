package com.example.grabapplication.googlemaps

import com.example.grabapplication.googlemaps.models.Distance
import com.google.android.gms.maps.model.LatLng

class MapsConstant {
    companion object {
        const val URL_DIRECTION = "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&key=%s"
        const val URL_FIND_PLACE = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&key=%s"
        const val URL_GEOCODE_PLACE = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s&key=%s"
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
        const val DIRECTION_START_ADDRESS = "start_address"
        const val DIRECTION_END_ADDRESS = "end_address"
        const val DIRECTION_START_LOCATION = "start_location"
        const val DIRECTION_END_LOCATION = "end_location"
        const val DIRECTION_LAT = "lat"
        const val DIRECTION_LNG = "lng"

        // google geocoding api key
        const val GEOCODE_STATUS = "status"
        const val GEOCODE_RESULTS = "results"
        const val GEOCODE_FORMATTED_ADDRESS = "formatted_address"

        // google place api key
        const val PLACE_STATUS = "status"
        const val PLACE_RESULTS = "results"
        const val PLACE_GEOMETRY = "geometry"
        const val PLACE_LOCATION = "location"
        const val PLACE_LAT = "lat"
        const val PLACE_LNG = "lng"
        const val PLACE_FORMATTED_ADDRESS = "formatted_address"
        const val PLACE_NAME = "name"

        val DEFAULT_DISTANCE = Distance("1,5 km", 1506, "5 phút", 322, "Ng. 106 - Hoàng Quốc Việt, Hà Nội, Việt Nam", "Ng. 10 Nguyễn Văn Huyên, Cầu Giấy, Hà Nội, Việt Nam", 21.0477757, 105.794652, 21.0376798, 105.7965187)
    }
}