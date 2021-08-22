package com.example.grabapplication.googlemaps

import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.model.BookInfo

class MapsConstant {
    companion object {
        const val URL_DIRECTION = "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&key=%s"
        const val URL_FIND_PLACE = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&region=vn&key=%s"
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

        val DEFAULT_DISTANCE = Distance("1,5 km", 1506, "5 phút", 322, "Ng. 106 - Hoàng Quốc Việt, Hà Nội, Việt Nam", "Ng. 10 Nguyễn Văn Huyên, Cầu Giấy, Hà Nội, Việt Nam", 21.0477757, 105.794652, 21.0376798, 105.7965187)
        val DEFAULT_BOOK_INFO = BookInfo(null, null, null, null, null, null, null, null, null, null)
    }
}