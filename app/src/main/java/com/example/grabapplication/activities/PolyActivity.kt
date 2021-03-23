package com.example.grabapplication.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.grabapplication.R
import com.example.grabapplication.googlemaps.MapsConnection
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PolyActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOWER_MANHATTAN = LatLng(20.9899, 105.7972)
    private val BROOKLYN_BRIDGE = LatLng(21.0047, 105.8221)
    private val WALL_STREET = LatLng(21.0081, 105.8273)

    private var googleMap: GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poly)

        // Build the map.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val options = MarkerOptions()
        options.position(WALL_STREET)
        googleMap.addMarker(options)

        MapsConnection.getInstance().drawShortestWay(googleMap, LOWER_MANHATTAN, WALL_STREET)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOWER_MANHATTAN, 13F))
    }
}