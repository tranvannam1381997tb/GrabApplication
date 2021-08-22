package com.example.grabapplication.googlemaps

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.manager.AccountManager
import org.json.JSONObject
import java.net.URLEncoder

class MapsConnection private constructor() {

    fun getShortestWay(latitude: Double, longitude: Double, callback: (Distance) -> Unit) {
        var min = 0
        var minDistance = MapsConstant.DEFAULT_DISTANCE
        val currentLocation = AccountManager.getInstance().getLocationUser()
        val urlDirections = getMapsApiDirectionsUrl(currentLocation.latitude, currentLocation.longitude, latitude, longitude)
        val directionsRequest = object : StringRequest(
            Method.GET,
            urlDirections,
            Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)

                val status = CommonUtils.getStringFromJsonObject(jsonResponse, MapsConstant.DIRECTION_STATUS)
                if (status == MapsConstant.STATUS_OK) {
                    // Get routes
                    val routes = CommonUtils.getJsonArrayFromJsonObject(
                        jsonResponse,
                        MapsConstant.DIRECTION_ROUTES
                    )

                    for (i in 0 until routes.length()) {
                        val route = routes.getJSONObject(i)

                        val legs = CommonUtils.getJsonArrayFromJsonObject(
                            route,
                            MapsConstant.DIRECTION_LEGS
                        )
                        for (j in 0 until legs.length()) {
                            val leg = legs.getJSONObject(j)

                            val distance = CommonUtils.getJsonObjectFromJsonObject(leg, MapsConstant.DIRECTION_DISTANCE)
                            val distanceValue = CommonUtils.getIntFromJsonObject(distance, MapsConstant.DIRECTION_VALUE)


                            if ((min == 0) || (min != 0 && distanceValue < min)) {
                                min = distanceValue

                                val distanceText = CommonUtils.getStringFromJsonObject(distance, MapsConstant.DIRECTION_TEXT)

                                val duration = CommonUtils.getJsonObjectFromJsonObject(leg, MapsConstant.DIRECTION_DURATION)
                                val durationText = CommonUtils.getStringFromJsonObject(duration, MapsConstant.DIRECTION_TEXT)
                                val durationValue = CommonUtils.getIntFromJsonObject(duration, MapsConstant.DIRECTION_VALUE)

                                val startAddress = CommonUtils.getStringFromJsonObject(leg, MapsConstant.DIRECTION_START_ADDRESS)
                                val endAddress = CommonUtils.getStringFromJsonObject(leg, MapsConstant.DIRECTION_END_ADDRESS)

                                val startLocation = CommonUtils.getJsonObjectFromJsonObject(leg, MapsConstant.DIRECTION_START_LOCATION)
                                val endLocation = CommonUtils.getJsonObjectFromJsonObject(leg, MapsConstant.DIRECTION_END_LOCATION)

                                val latStart = CommonUtils.getDoubleFromJsonObject(startLocation, MapsConstant.DIRECTION_LAT)
                                val lngStart = CommonUtils.getDoubleFromJsonObject(startLocation, MapsConstant.DIRECTION_LNG)

                                val latEnd = CommonUtils.getDoubleFromJsonObject(endLocation, MapsConstant.DIRECTION_LAT)
                                val lngEnd = CommonUtils.getDoubleFromJsonObject(endLocation, MapsConstant.DIRECTION_LNG)

                                minDistance = Distance(distanceText, distanceValue, durationText, durationValue, startAddress, endAddress, latStart, lngStart, latEnd, lngEnd)
                            }
                        }
                    }

                }
                callback.invoke(minDistance)
            },
            Response.ErrorListener {
            }){}
        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(directionsRequest)
    }

    private fun getMapsApiDirectionsUrl(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): String {
        val originLocation = "$startLatitude,$startLongitude"
        val destinationLocation = "$endLatitude,$endLongitude"
        return String.format(
            MapsConstant.URL_DIRECTION,
            originLocation,
            destinationLocation,
            GrabApplication.getAppContext().getString(
                R.string.maps_api_key
            )
        )
    }

    private fun getMapsApiPlaceUrl(place: String): String {
        val placeEncode = encodeURL(place)
        return String.format(
            MapsConstant.URL_FIND_PLACE, placeEncode, GrabApplication.getAppContext().getString(
                R.string.maps_api_key
            )
        )
    }

    private fun encodeURL(place: String): String {
        try {
            return URLEncoder.encode(place, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    companion object {
        private var instance: MapsConnection? = null
        fun getInstance(): MapsConnection {
            if (instance == null) {
                synchronized(MapsConnection::class.java) {
                    if (instance == null) {
                        instance = MapsConnection()
                    }
                }
            }
            return instance!!
        }
    }
}