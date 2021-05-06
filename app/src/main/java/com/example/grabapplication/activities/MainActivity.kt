package com.example.grabapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.*
import com.example.grabapplication.customviews.ConfirmDialog
import com.example.grabapplication.databinding.ActivityMainBinding
import com.example.grabapplication.firebase.FirebaseConnection
import com.example.grabapplication.firebase.FirebaseManager
import com.example.grabapplication.fragments.FindPlaceFragment
import com.example.grabapplication.fragments.InfoDriverFragment
import com.example.grabapplication.googlemaps.MapsConnection
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    
    private var map: GoogleMap? = null

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel
            by lazy {
                ViewModelProvider(this, BaseViewModelFactory(this)).get(
                    MainViewModel::class.java
                )
            }

    private val driverManager: DriverManager
            by lazy {
                DriverManager.getInstance()
            }

    private val accountManager: AccountManager
            by lazy {
                AccountManager.getInstance()
            }

    private lateinit var transaction: FragmentTransaction

    // The entry point to the Fused Location Provider.
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private var locationPermissionGranted = false

    private var currentFragment : Fragment? = null

    private var driverHashMap: HashMap<String, Marker> = HashMap()
    private var listDriver: HashMap<String, DriverInfo> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mainViewModel


        // TODO debug code
        accountManager.saveIdUser("idUser_1")

        initDataMap()
        initView()
        accountManager.getTokenIdDevice {  }

        // TODO debug code

        driverManager.addListIdDriver()
        getInfoDriver(FirebaseManager.getInstance().databaseDrivers)

    }

    private fun initDataMap() {
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initView() {
        // Build the map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        transaction = supportFragmentManager.beginTransaction()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        mainViewModel.onItemClickListener = object : MainViewModel.OnItemClickListener {
            override fun openFindPlaceFragment() {
                gotoFindPlaceFragment()
            }

            override fun bookDriver() {
                showDialogConfirmBookDriver()
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("NamTV", "onMapReady")
        map = googleMap
        map!!.setOnMarkerClickListener(this)

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        if (locationPermissionGranted) {
            // Get the current location of the device and set the position of the map.
            getDeviceLocation()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean{
        val idDriver = marker?.tag
        if (idDriver != null && listDriver.containsKey(idDriver)) {
            val driverInfo = listDriver[idDriver]
            selectDriver(driverInfo!!)
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    getDeviceLocation()
                    updateLocationUI()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        if (currentFragment is InfoDriverFragment) {
            mainViewModel.isShowMapLayout.set(true)
            removeInfoDriverFragment()
            return
        }

        super.onBackPressed()
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                locationRequest.interval = (30*1000).toLong()
                locationRequest.fastestInterval = (30*1000).toLong()
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        if (locationResult == null) {
                            return
                        }
                        for (location in locationResult.locations) {
                            if (location != null) {
                                val currentLocation = LatLng(location.latitude, location.longitude)
                                accountManager.setLocationUser(currentLocation)
                                map?.moveCamera(
                                    CameraUpdateFactory.newLatLng(currentLocation)
                                )
                            }
                        }
                    }
                }

                fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        accountManager.setLocationUser(currentLocation)
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocation, Constants.DEFAULT_ZOOM_MAPS.toFloat()
                            )
                        )
                        fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("NamTV", "MainActivity::getDeviceLocation: SecurityException: $e")
        }
    }

    private fun addOrUpdateMarkerDriver(driverInfo: DriverInfo) {
        if (driverHashMap.containsKey(driverInfo.idDriver)) {
            val marker = driverHashMap[driverInfo.idDriver]
            marker?.position = LatLng(driverInfo.latitude, driverInfo.longitude)
        } else {
            val markerOption = MarkerOptions().apply {
                position(LatLng(driverInfo.latitude, driverInfo.longitude))
                icon(bitmapFromVector(R.drawable.motocross))
                title(driverInfo.name)
                snippet(driverInfo.rate.toString())
            }
            val marker = map!!.addMarker(markerOption)
            marker.tag = driverInfo.idDriver
            driverHashMap[driverInfo.idDriver] = marker
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            locationPermissionGranted = true
        }
    }

    private fun bitmapFromVector(vectorResId: Int): BitmapDescriptor? {
        // below line is use to generate a drawable.
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)

        // below line is use to set bounds to our vector drawable.
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        // below line is use to create a bitmap for our
        // drawable which we have added.
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // below line is use to add bitmap in our canvas.
        val canvas = Canvas(bitmap)

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas)

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                accountManager.setLocationUser(Constants.DEFAULT_LOCATION)
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("NamTV", "MainActivity::updateLocationUI: SecurityException: $e")
        }
    }

    private fun getInfoDriver(database: DatabaseReference) {
        for (id in driverManager.listIdDriver) {
            database.child(id).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("NamTV", "onDataChange")
                    val driverInfo = driverManager.getInfoDriverFromDataSnapshot(snapshot)
                    listDriver[driverInfo.idDriver] = driverInfo
                    addOrUpdateMarkerDriver(driverInfo)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("NamTV", "onCancelled")
                }
            })
        }
    }

    private fun selectDriver(driverInfo: DriverInfo) {
        mainViewModel.selectDriver(driverInfo) {
            currentFragment = InfoDriverFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_top,
                R.anim.pop_in_bottom,
                R.anim.pop_out_top
            )
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragmentBook, currentFragment as InfoDriverFragment).commit()
            mainViewModel.isShowMapLayout.set(false)
        }
    }

    private fun removeInfoDriverFragment() {
        if (currentFragment !is InfoDriverFragment) {
            return
        }
        mainViewModel.driverInfoSelect = null

        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.remove(currentFragment as InfoDriverFragment).commit()
        currentFragment = null
    }

    private fun gotoFindPlaceFragment() {
        currentFragment = FindPlaceFragment()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.addToBackStack(null)
        transaction.add(R.id.fragmentBook, currentFragment as FindPlaceFragment).commit()
        mainViewModel.isShowMapLayout.set(false)
    }

    private fun showDialogConfirmBookDriver() {
        val dialogConfirm = ConfirmDialog(this)
        dialogConfirm.setTextDisplay(
            getString(R.string.confirm_book_driver),
            null,
            getString(R.string.label_cancel),
            getString(R.string.label_ok)
        )
        dialogConfirm.setOnClickOK(View.OnClickListener {
            mainViewModel.distancePlaceChoose.get()?.let { distance ->
                FirebaseConnection.getInstance().pushNotifyToDriver(distance, mainViewModel.driverInfoSelect!!.tokenId)
            }

            dialogConfirm.dismiss()
        })
        dialogConfirm.setTextTypeBoldBtnOK()
        dialogConfirm.show()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}