package com.example.grabapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.AccountManager
import com.example.grabapplication.common.Constants
import com.example.grabapplication.common.DriverManager
import com.example.grabapplication.customviews.ConfirmDialog
import com.example.grabapplication.databinding.ActivityMainBinding
import com.example.grabapplication.firebase.FirebaseConnection
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.fragments.*
import com.example.grabapplication.googlemaps.MapsUtils
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.services.BookListener
import com.example.grabapplication.services.GrabFirebaseMessagingService
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

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

    private var fragmentBottom : Fragment? = null
    var currentFragment = Constants.FRAGMENT_MAP

    private var isFirstGetDeviceLocation = true

    private var timeDriverArrivedDestination = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mainViewModel

        initDataMap()
        initView()
        setupEvent()
        accountManager.getTokenIdDevice {  }
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

        mainViewModel.onItemClickListener = object : MainViewModel.OnItemClickListener {
            override fun openFindPlaceFragment() {
                gotoFindPlaceFragment()
            }

            override fun bookDriver() {
                showDialogConfirmBookDriver()
            }
        }
    }

    private fun setupEvent() {
        GrabFirebaseMessagingService.bookListener = object : BookListener {
            override fun handleDriverGoingBook(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_WAIT_DRIVER && fragmentBottom is WaitDriverFragment) {
                    (fragmentBottom as WaitDriverFragment).countDownTimer?.cancel()
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVING_ORIGIN, jsonData)
                    }
                }
            }

            override fun handleDriverReject() {
                if (currentFragment == Constants.FRAGMENT_WAIT_DRIVER && fragmentBottom is WaitDriverFragment) {
                    (fragmentBottom as WaitDriverFragment).showDialogBookNew()
                }
            }

            override fun handleDriverArrivedOrigin(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVED_ORIGIN, jsonData)
                        timeDriverArrivedDestination = jsonData.getInt(FirebaseConstants.KEY_TIME_ARRIVED_DESTINATION)
                    }
                }
            }

            override fun handleDriverGoing(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_START_GOING, jsonData)
                    }
                }
            }

            override fun handleDriverArrivedDestination(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVED_DESTINATION, jsonData)
                    }
                }
            }

            override fun handleDriverBill() {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoBillFragment()
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("NamTV", "onMapReady")
        mainMap = googleMap
        mainMap!!.setOnMarkerClickListener(this)

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
        if (idDriver != null && driverManager.listDriverHashMap.containsKey(idDriver)) {
            val driverInfo = driverManager.listDriverHashMap[idDriver]
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
        if (currentFragment == Constants.FRAGMENT_INFO_DRIVER) {
            currentFragment = Constants.FRAGMENT_MAP
            gotoMapFragment()
            return
        }
        if (currentFragment == Constants.FRAGMENT_WAIT_DRIVER && fragmentBottom is WaitDriverFragment) {
            (fragmentBottom as WaitDriverFragment).showDialogConfirmCancelBook()
            return
        }
        if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
            showDialogConfirmCancelBook()
            return
        }
        if (currentFragment == Constants.FRAGMENT_FIND_PLACE) {
            currentFragment = Constants.FRAGMENT_INFO_DRIVER
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
                                mainMap?.moveCamera(
                                    CameraUpdateFactory.newLatLng(currentLocation)
                                )
                                if (isFirstGetDeviceLocation) {
                                    isFirstGetDeviceLocation = false
                                    driverManager.getListDriverFromServer()
                                    Log.d("NamTV", "isFirstGetDeviceLocation")
                                }
                            }
                        }
                    }
                }

                fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        accountManager.setLocationUser(currentLocation)
                        mainMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocation, Constants.DEFAULT_ZOOM_MAPS.toFloat()
                            )
                        )
                        if (isFirstGetDeviceLocation) {
                            isFirstGetDeviceLocation = false
                            driverManager.getListDriverFromServer()
                            Log.d("NamTV", "isFirstGetDeviceLocation")
                        }
                        fusedLocationProviderClient?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("NamTV", "MainActivity::getDeviceLocation: SecurityException: $e")
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            locationPermissionGranted = true
        }
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
                mainMap?.isMyLocationEnabled = true
                mainMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mainMap?.isMyLocationEnabled = false
                mainMap?.uiSettings?.isMyLocationButtonEnabled = false
                accountManager.setLocationUser(Constants.DEFAULT_LOCATION)
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("NamTV", "MainActivity::updateLocationUI: SecurityException: $e")
        }
    }

    private fun selectDriver(driverInfo: DriverInfo) {
        mainViewModel.selectDriver(driverInfo) {
            mainViewModel.isShowingLayoutBottom.set(true)
            gotoInfoDriverFragment()
        }
    }

    fun gotoMapFragment() {
        currentFragment = Constants.FRAGMENT_MAP
        fragmentBottom = null
        mainViewModel.isShowingLayoutBottom.set(false)
        for (fragment in supportFragmentManager.fragments) {
            if (fragment !is SupportMapFragment) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
        }
    }

    private fun gotoInfoDriverFragment() {
        fragmentBottom = InfoDriverFragment()
        currentFragment = Constants.FRAGMENT_INFO_DRIVER
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_top,
                R.anim.pop_in_bottom,
                R.anim.pop_out_top
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.fragmentBottom, fragmentBottom as InfoDriverFragment).commit()
        updateSizeFragmentBook()

    }

    private fun gotoFindPlaceFragment() {
        fragmentBottom = FindPlaceFragment()
        currentFragment = Constants.FRAGMENT_FIND_PLACE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.addToBackStack(null)
        transaction.add(R.id.fragmentBottom, fragmentBottom as FindPlaceFragment).commit()

        updateSizeFragmentBook()
    }

    private fun gotoWaitDriverFragment() {
        fragmentBottom = WaitDriverFragment()
        currentFragment = Constants.FRAGMENT_WAIT_DRIVER
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.replace(R.id.fragmentBottom, fragmentBottom as WaitDriverFragment).commit()
        updateSizeFragmentBook()
    }

    fun gotoDriverGoingFragment(statusDriverGoingFragment: Int, jsonData: JSONObject) {
        fragmentBottom = DriverGoingFragment()
        currentFragment = Constants.FRAGMENT_DRIVER_GOING

        val bundle = Bundle()
        bundle.putInt(DriverGoingFragment.STATUS_DRIVER_GOING_FRAGMENT, statusDriverGoingFragment)
        bundle.putString(DriverGoingFragment.JSON_DATA, jsonData.toString())
        bundle.putInt(FirebaseConstants.KEY_TIME_ARRIVED_DESTINATION, timeDriverArrivedDestination)
        fragmentBottom!!.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.replace(R.id.fragmentBottom, fragmentBottom as DriverGoingFragment).commit()
        updateSizeFragmentBook()
    }

    private fun gotoBillFragment() {
        fragmentBottom = BillFragment()
        currentFragment = Constants.FRAGMENT_BILL
        mainViewModel.isShowingLayoutBill.set(true)
        mainViewModel.isShowingLayoutBottom.set(false)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.replace(R.id.fragmentBill, fragmentBottom as BillFragment).commit()
    }

    private fun updateSizeFragmentBook() {
        val layoutParams = binding.fragmentBottom.layoutParams
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        binding.fragmentBottom.layoutParams = layoutParams
    }

    fun showDialogConfirmCancelBook() {
        val dialogConfirm = ConfirmDialog(this)
        dialogConfirm.setTextDisplay(
            getString(R.string.confirm_cancel_book),
            null,
            getString(R.string.no),
            getString(R.string.cancel_book)
        )
        dialogConfirm.setOnClickOK(View.OnClickListener {
            dialogConfirm.dismiss()
            gotoMapFragment()

            // TODO
        })
        dialogConfirm.setOnClickCancel(View.OnClickListener {
            dialogConfirm.dismiss()
        })
        dialogConfirm.setTextTypeBoldBtnOK()
        dialogConfirm.show()
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
            FirebaseConnection.getInstance().pushNotifyToDriver(mainViewModel.bookInfo.get()!!) { isSuccess ->
                if (isSuccess) {
                    gotoWaitDriverFragment()
                } else {
                    showDialogError(getString(R.string.error_connect_to_driver))
                }
            }

            dialogConfirm.dismiss()
        })
        dialogConfirm.setTextTypeBoldBtnOK()
        dialogConfirm.show()
    }

    private fun showDialogError(error: String) {
        val dialogError = ConfirmDialog(this)
        dialogError.setTextDisplay(
            error,
            null,
            getString(R.string.back),
            getString(R.string.try_again)
        )
        dialogError.setOnClickOK(View.OnClickListener {
            dialogError.dismiss()
        })
        dialogError.setOnClickCancel(View.OnClickListener {
            dialogError.dismiss()
            gotoMapFragment()
        })
        dialogError.show()
    }

    private fun saveBookInfo() {
       // TODO
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private var mainMap: GoogleMap? = null
        private var driverHashMap: HashMap<String, Marker> = HashMap()
        fun addOrUpdateMarkerDriver(driverInfo: DriverInfo) {
            if (driverHashMap.containsKey(driverInfo.driverId)) {
                // Update marker
                val marker = driverHashMap[driverInfo.driverId]
                marker?.position = LatLng(driverInfo.latitude, driverInfo.longitude)
                Log.d("NamTV", "Update marker")
            } else {
                // Add new marker
                val markerOption = MarkerOptions().apply {
                    position(LatLng(driverInfo.latitude, driverInfo.longitude))
                    icon(MapsUtils.bitmapFromVector(R.drawable.motocross))
                    title(driverInfo.name)
                    snippet(driverInfo.rate.toString())
                }
                val marker = mainMap!!.addMarker(markerOption)
                marker.tag = driverInfo.driverId
                driverHashMap[driverInfo.driverId] = marker
                Log.d("NamTV", "Add new marker")
            }
        }
    }
}