package com.example.grabapplication.googlemaps

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.googlemaps.models.PlaceModel
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.collections.ArrayList

class MapsConnection private constructor() {

//    fun drawShortestWay(googleMap: GoogleMap, latitude: Double, longitude: Double) {
//        var maxDistance = 0
//        val currentLocation = MainActivity.currentLocation
//        val urlDirections = getMapsApiDirectionsUrl(currentLocation.latitude, currentLocation.longitude, latitude, longitude)
//        val path: MutableList<List<LatLng>> = ArrayList()
//        val directionsRequest = object : StringRequest(
//            Method.GET,
//            urlDirections,
//            Response.Listener<String> { response ->
//                val jsonResponse = JSONObject(response)
//                // Get routes
//                val routes = CommonUtils.getJsonArrayFromJsonObject(
//                    jsonResponse,
//                    MapsConstant.DIRECTION_ROUTES
//                )
//                val legs = CommonUtils.getJsonArrayFromJsonObject(
//                    routes.getJSONObject(0),
//                    MapsConstant.DIRECTION_LEGS
//                )
//                val step = CommonUtils.getJsonArrayFromJsonObject(
//                    legs.getJSONObject(0),
//                    MapsConstant.DIRECTION_STEPS
//                )
//                for (i in 0 until step.length()) {
//                    val polyline = CommonUtils.getJsonObjectFromJsonObject(
//                        step.getJSONObject(i),
//                        MapsConstant.DIRECTION_POLYLINE
//                    )
//                    val points = CommonUtils.getStringFromJsonObject(
//                        polyline,
//                        MapsConstant.DIRECTION_POINTS
//                    )
//                    path.add(PolyUtil.decode(points))
//
//                    val distance = CommonUtils.getJsonObjectFromJsonObject(
//                        step.getJSONObject(i),
//                        "distance"
//                    )
//                    val value = CommonUtils.getFloatFromJsonObject(distance, "value")
//                    if (maxDistance < value) {
//                        maxDistance = value.toInt()
//                    }
//                }
//                for (i in 0 until path.size) {
//                    googleMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
//                }
//            },
//            Response.ErrorListener {
//            }){}
//        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
//        requestQueue.add(directionsRequest)
//    }

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

                                minDistance = Distance(distanceText, distanceValue, durationText, durationValue, startAddress, endAddress)
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
                R.string.directions_api_key
            )
        )
    }

    fun findPlace(place: String, callback: (ArrayList<PlaceModel>) -> Unit) {
        val listPlace = ArrayList<PlaceModel>()
        val urlString = getMapsApiPlaceUrl(place)
        val placeRequest =
            object : StringRequest(Method.GET, urlString, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                val status =
                    CommonUtils.getStringFromJsonObject(jsonResponse, MapsConstant.PLACE_STATUS)
                if (status == MapsConstant.STATUS_OK) {
                    val result = CommonUtils.getJsonArrayFromJsonObject(
                        jsonResponse,
                        MapsConstant.PLACE_RESULTS
                    )
                    for (i in 0 until result.length()) {
                        val geometry = CommonUtils.getJsonObjectFromJsonObject(
                            result.getJSONObject(i),
                            MapsConstant.PLACE_GEOMETRY
                        )
                        val location = CommonUtils.getJsonObjectFromJsonObject(
                            geometry,
                            MapsConstant.PLACE_LOCATION
                        )
                        val lat =
                            CommonUtils.getDoubleFromJsonObject(
                                location,
                                MapsConstant.PLACE_LAT
                            )
                        val lng =
                            CommonUtils.getDoubleFromJsonObject(
                                location,
                                MapsConstant.PLACE_LNG
                            )
                        val formattedAddress = CommonUtils.getStringFromJsonObject(
                            result.getJSONObject(i),
                            MapsConstant.PLACE_FORMATTED_ADDRESS
                        )
                        val name = CommonUtils.getStringFromJsonObject(
                            result.getJSONObject(i),
                            MapsConstant.PLACE_NAME
                        )
                        val placeModel = PlaceModel(lat, lng, formattedAddress, name)
                        listPlace.add(placeModel)
                    }
                }
                callback.invoke(listPlace)
            },
        Response.ErrorListener {
        }) {}

        val requestQueue = Volley.newRequestQueue(GrabApplication.getAppContext())
        requestQueue.add(placeRequest)
    }

    private fun getMapsApiPlaceUrl(place: String): String {
        val placeEncode = encodeURL(place)
        return String.format(
            MapsConstant.URL_FIND_PLACE, placeEncode, GrabApplication.getAppContext().getString(
                R.string.directions_api_key
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