package com.example.grabapplication.googlemaps

import android.graphics.Color
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrapApplication
import com.example.grabapplication.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject

class MapsConnection private constructor() {

    fun drawShortestWay(googleMap: GoogleMap, latLngOrigin : LatLng, latLngDestination : LatLng) {
        val urlDirections = getMapsApiDirectionsUrl(latLngOrigin, latLngDestination)
        val path: MutableList<List<LatLng>> = ArrayList()
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
            response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for (i in 0 until path.size) {
                googleMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
        }){}
        val requestQueue = Volley.newRequestQueue(GrapApplication.getAppContext())
        requestQueue.add(directionsRequest)
    }


    private fun getMapsApiDirectionsUrl(latLngOrigin : LatLng, latLngDestination : LatLng): String {
        val originLocation = "${latLngOrigin.latitude},${latLngOrigin.longitude}"
        val destinationLocation = "${latLngDestination.latitude},${latLngDestination.longitude}"
        return String.format(MapsConstant.URL_DIRECTION, originLocation, destinationLocation, GrapApplication.getAppContext().getString(R.string.directions_api_key))
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