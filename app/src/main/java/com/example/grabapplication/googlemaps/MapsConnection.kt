package com.example.grabapplication.googlemaps

import android.content.Context
import android.graphics.Color
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.grabapplication.GrapApplication
import com.example.grabapplication.R
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.googlemaps.models.DirectionResponses
import com.example.grabapplication.googlemaps.models.PlaceModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLEncoder

class MapsConnection private constructor() {

    fun drawShortestWay(googleMap: GoogleMap, startAddress : String, endAddress : String) {
        var maxDistance = 0
        val urlDirections = getMapsApiDirectionsUrl(startAddress, endAddress)
        val path: MutableList<List<LatLng>> = ArrayList()
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
            response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = CommonUtils.getJsonArrayFromJsonObject(jsonResponse, MapsConstant.DIRECTION_ROUTES)
            val legs = CommonUtils.getJsonArrayFromJsonObject(routes.getJSONObject(0), MapsConstant.DIRECTION_LEGS)
            val step = CommonUtils.getJsonArrayFromJsonObject(legs.getJSONObject(0), MapsConstant.DIRECTION_STEPS)
            for (i in 0 until step.length()) {
                val polyline = CommonUtils.getJsonObjectFromJsonObject(step.getJSONObject(i), MapsConstant.DIRECTION_POLYLINE)
                val points = CommonUtils.getStringFromJsonObject(polyline, MapsConstant.DIRECTION_POINTS)
                path.add(PolyUtil.decode(points))

                val distance = CommonUtils.getJsonObjectFromJsonObject(step.getJSONObject(i), "distance")
                val value = CommonUtils.getFloatFromJsonObject(distance, "value")
                if ( maxDistance < value) {
                    maxDistance = value.toInt()
                }
            }
            for (i in 0 until path.size) {
                googleMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }


        }, Response.ErrorListener {
        }){}
        val requestQueue = Volley.newRequestQueue(GrapApplication.getAppContext())
        requestQueue.add(directionsRequest)
    }

    fun findPlace(place: String): ArrayList<PlaceModel> {
        val listPlace = ArrayList<PlaceModel>()
        val urlGeocoding = getMapsApiPlaceUrl(place)
        val geocodingRequest = object : StringRequest(Method.GET, urlGeocoding, Response.Listener<String> {
            response ->
            val jsonResponse = JSONObject(response)
            val result = CommonUtils.getJsonArrayFromJsonObject(jsonResponse, MapsConstant.GEOCODING_RESULTS)
            for (i in 0 until result.length()) {
                val geometry = CommonUtils.getJsonObjectFromJsonObject(result.getJSONObject(i), MapsConstant.GEOCODING_GEOMETRY)
                val location = CommonUtils.getJsonObjectFromJsonObject(geometry, MapsConstant.GEOCODING_LOCATION)
                val lat = CommonUtils.getFloatFromJsonObject(location, MapsConstant.GEOCODING_LAT)
                val lng = CommonUtils.getFloatFromJsonObject(location, MapsConstant.GEOCODING_LNG)
                val formattedAddress = CommonUtils.getStringFromJsonObject(result.getJSONObject(i), MapsConstant.GEOCODING_FORMATTED_ADDRESS)
                val placeModel = PlaceModel(lat, lng, formattedAddress)
                listPlace.add(placeModel)
            }
        }, Response.ErrorListener {
        }){}

        return listPlace
    }

    private fun getMapsApiDirectionsUrl(startAddress : String, endAddress : String): String {
        val originLocation = URLEncoder.encode(startAddress, "UTF-8")
        val destinationLocation = URLEncoder.encode(endAddress, "UTF-8")
        return String.format(MapsConstant.URL_DIRECTION, originLocation, destinationLocation, GrapApplication.getAppContext().getString(R.string.directions_api_key))
    }

    private fun getMapsApiPlaceUrl(place: String): String {
        val placeEncode = URLEncoder.encode(place, MapsConstant.URL_ENCODE_CHARSETS)
        return String.format(MapsConstant.URL_FIND_PLACE, placeEncode, GrapApplication.getAppContext().getString(R.string.directions_api_key))
    }

//    fun drawShortestWay(googleMap: GoogleMap, latLngOrigin : LatLng, latLngDestination : LatLng) {
//        val originLocation = "${latLngOrigin.latitude},${latLngOrigin.longitude}"
//        val destinationLocation = "${latLngDestination.latitude},${latLngDestination.longitude}"
//        val apiServices = RetrofitClient.apiServices(GrapApplication.getAppContext())
//        apiServices.getDirection(originLocation, destinationLocation, GrapApplication.getAppContext().getString(R.string.directions_api_key))
//            .enqueue(object : Callback<DirectionResponses> {
//                override fun onResponse(call: Call<DirectionResponses>, response: retrofit2.Response<DirectionResponses>) {
//                    drawPolyline(googleMap, response)
//                    Log.d("NamTV", response.message())
//                }
//
//                override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
//                    Log.e("NamTV", t.localizedMessage)
//                }
//            })
//    }

    private fun drawPolyline(googleMap: GoogleMap, response: retrofit2.Response<DirectionResponses>) {
        val shape = response.body()?.routes?.get(0)?.overviewPolyline?.points
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(shape))
            .width(8f)
            .color(Color.RED)
        googleMap.addPolyline(polyline)
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

    private object RetrofitClient {
        fun apiServices(context: Context): ApiServices {
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(MapsConstant.BASE_URL_GOOGLE_MAP)
                    .build()

            return retrofit.create<ApiServices>(ApiServices::class.java)
        }
    }

    private interface ApiServices {
        @GET("maps/api/directions/json")
        fun getDirection(@Query("origin") origin: String,
                         @Query("destination") destination: String,
                         @Query("key") apiKey: String): Call<DirectionResponses>
    }
}

interface GetDistanceListener {
    fun getDistance()
}