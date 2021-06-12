package com.example.grabapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.GrabApplication
import com.example.grabapplication.R
import com.example.grabapplication.common.AppPreferences
import com.example.grabapplication.common.Constants
import com.example.grabapplication.common.DriverManager
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.customviews.ConfirmDialog
import com.example.grabapplication.databinding.ActivityMainBinding
import com.example.grabapplication.firebase.FirebaseConnection
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.fragments.*
import com.example.grabapplication.googlemaps.MapsUtils
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.DriverStatus
import com.example.grabapplication.model.TypeDriverValue
import com.example.grabapplication.services.BookListener
import com.example.grabapplication.services.GrabFirebaseMessagingService
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

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

    private lateinit var transaction: FragmentTransaction

    // The entry point to the Fused Location Provider.
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private var locationPermissionGranted = false

    private var fragmentBottom : Fragment? = null

    private var isFirstGetDeviceLocation = true

    private var timeDriverArrivedDestination = 0

    var isChoosingDriver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mainViewModel

        initDataMap()
        initView()
        setupEvent()
        setupDrawerLayout()
        mainViewModel.accountManager.getTokenIdDevice {  }
        getDataBook()
    }

    private fun initDataMap() {
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val apiKey = getString(R.string.maps_api_key)

        if (!Places.isInitialized()) {
            Places.initialize(GrabApplication.getAppContext(), apiKey)
        }

        Places.createClient(this)
    }

    private fun initView() {
        // Build the map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        transaction = supportFragmentManager.beginTransaction()
    }

    private fun setupDrawerLayout() {
        findViewById<TextView>(R.id.layoutLeftTxtName).text = mainViewModel.accountManager.getName()
        findViewById<TextView>(R.id.layoutLeftTxtAge).text = getString(R.string.age, mainViewModel.accountManager.getAge().toString())
        findViewById<TextView>(R.id.layoutLeftTxtSex).text = getString(R.string.sex_info, mainViewModel.accountManager.getSex())
        findViewById<TextView>(R.id.layoutLeftTxtPhoneNumber).text = getString(R.string.phone_number_info, mainViewModel.accountManager.getPhoneNumber())
        findViewById<ImageView>(R.id.imgLogout).setOnSingleClickListener(View.OnClickListener {
            HttpConnection.getInstance().logout {
                Log.d("NamTV", "logout = $it")
                if (it) {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun setupEvent() {
        GrabFirebaseMessagingService.bookListener = object : BookListener {
            override fun handleDriverGoingBook(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_WAIT_DRIVER && fragmentBottom is WaitDriverFragment) {
                    (fragmentBottom as WaitDriverFragment).countDownTimer?.cancel()
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVING_ORIGIN, jsonData)
                        saveBookInfo(jsonData)
                    }
                }
            }

            override fun handleDriverReject() {
                if (currentFragment == Constants.FRAGMENT_WAIT_DRIVER && fragmentBottom is WaitDriverFragment) {
                    this@MainActivity.runOnUiThread {
                        (fragmentBottom as WaitDriverFragment).showDialogBookNew()
                        AppPreferences.getInstance(this@MainActivity).bookInfoPreferences = null
                    }
                }
            }

            override fun handleDriverArrivedOrigin(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVED_ORIGIN, jsonData)
                        timeDriverArrivedDestination = jsonData.getInt(FirebaseConstants.KEY_TIME_ARRIVED_DESTINATION)
                        saveBookInfo(jsonData)
                    }
                }
            }

            override fun handleDriverGoing(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVING_DESTINATION, jsonData)
                        saveBookInfo(jsonData)
                    }
                }
            }

            override fun handleDriverArrivedDestination(jsonData: JSONObject) {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVED_DESTINATION, jsonData)
                        saveBookInfo(jsonData)
                    }
                }
            }

            override fun handleDriverBill() {
                if (currentFragment == Constants.FRAGMENT_DRIVER_GOING && fragmentBottom is DriverGoingFragment) {
                    this@MainActivity.runOnUiThread {
                        AppPreferences.getInstance(GrabApplication.getAppContext()).bookInfoPreferences = null
                        gotoBillFragment()
                        driverManager.removeEventListenerStatusHistory()
                    }
                }
            }
        }

        mainViewModel.onItemClickListener = object : MainViewModel.OnItemClickListener {
            override fun openFindPlaceFragment() {
                gotoFindPlaceFragment()
            }

            override fun bookDriver() {
                showDialogConfirmBookDriver()
            }

            override fun endBook() {
                // Bill Fragment worked
            }

            override fun clickIconPhone() {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:${mainViewModel.bookInfo.get()!!.driverInfo!!.phoneNumber}")
                startActivity(callIntent)
            }
        }

        binding.iconBack.setOnSingleClickListener(View.OnClickListener {
            onBackPressed()
        })
        binding.imgInfo.setOnSingleClickListener(View.OnClickListener {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.drawerLayout.openDrawer(binding.menuLeft)
        })

        mainViewModel.isChoosingGrabBike.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateLayoutChooseTypeDriver()
            }
        })

        binding.txtGrabBike.paintFlags = binding.txtGrabBike.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun updateLayoutChooseTypeDriver() {
        if (mainViewModel.isChoosingGrabBike.get()!!) {
            setTextEnable(binding.txtGrabBike)
            setTextDisable(binding.txtGrabCar)
        } else {
            setTextDisable(binding.txtGrabBike)
            setTextEnable(binding.txtGrabCar)
        }
    }

    private fun setTextDisable(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.color_sign_up))
        textView.paintFlags = binding.txtGrabBike.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        textView.setTypeface(textView.typeface, Typeface.NORMAL)
    }

    private fun setTextEnable(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.color_sign_up))
        textView.paintFlags = binding.txtGrabBike.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        textView.setTypeface(textView.typeface, Typeface.BOLD)
    }

    private fun getDataBook() {
        val appPreferences = AppPreferences.getInstance(this)
        val bookInfoPreferences = appPreferences.bookInfoPreferences
        val jsonData = appPreferences.jsonDataBookInfo
        if (bookInfoPreferences != null) {
            mainViewModel.bookInfo.set(bookInfoPreferences)
            val driverInfo = bookInfoPreferences.driverInfo!!
            driverManager.getStatusHistoryBookInfo(driverInfo) {
                if (it) {
                    when(driverInfo.status) {
                        DriverStatus.StatusArrivingOrigin.rawValue -> {
                            gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVING_ORIGIN, jsonData)
                        }
                        DriverStatus.StatusArrivedOrigin.rawValue -> {
                            gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVED_ORIGIN, jsonData)
                        }
                        DriverStatus.StatusArrivingDestination.rawValue -> {
                            gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVING_DESTINATION, jsonData)
                        }
                        DriverStatus.StatusArrivedDestination.rawValue -> {
                            gotoDriverGoingFragment(DriverGoingFragment.STATUS_ARRIVED_DESTINATION, jsonData)
                        }
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
        if (currentFragment == Constants.FRAGMENT_MAP || currentFragment == Constants.FRAGMENT_DRIVER_SUGGEST ||
            currentFragment == Constants.FRAGMENT_INFO_DRIVER) {
            val idDriver = marker?.tag
            if (idDriver != null && driverManager.listDriverHashMap.containsKey(idDriver) && !isChoosingDriver) {
                isChoosingDriver = true
                val driverInfo = driverManager.listDriverHashMap[idDriver]
                selectDriver(driverInfo!!) {
                    isChoosingDriver = false
                }
            }
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
        if (currentFragment == Constants.FRAGMENT_INFO_DRIVER || currentFragment == Constants.FRAGMENT_DRIVER_SUGGEST) {
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
                                mainViewModel.accountManager.setLocationUser(currentLocation)
                                mainMap?.moveCamera(
                                    CameraUpdateFactory.newLatLng(currentLocation)
                                )
                                if (isFirstGetDeviceLocation) {
                                    isFirstGetDeviceLocation = false
                                    driverManager.getListDriverFromServer {

                                    }
                                    Log.d("NamTV", "isFirstGetDeviceLocation")
                                }
                            }
                        }
                    }
                }

                fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mainViewModel.accountManager.setLocationUser(currentLocation)
                        mainMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLocation, Constants.DEFAULT_ZOOM_MAPS.toFloat()
                            )
                        )
                        if (isFirstGetDeviceLocation) {
                            isFirstGetDeviceLocation = false
                            driverManager.getListDriverFromServer {
                                if (it) {
                                    scheduleGotoDriverSuggestFragment()
                                }
                            }
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
                mainViewModel.accountManager.setLocationUser(Constants.DEFAULT_LOCATION)
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("NamTV", "MainActivity::updateLocationUI: SecurityException: $e")
        }
    }

    fun selectDriver(driverInfo: DriverInfo, callback: (Boolean) -> Unit) {
        mainViewModel.selectDriver(driverInfo) {
            callback.invoke(true)
            gotoInfoDriverFragment()
        }
        mainViewModel.isShowingProgress.set(true)
    }

    fun gotoMapFragment() {
        mainViewModel.isShowingLayoutBill.set(false)
        mainViewModel.isShowingLayoutBottom.set(false)
        mainViewModel.isShowingIconBack.set(false)
        showingOtherDriver = true

        currentFragment = Constants.FRAGMENT_MAP
        fragmentBottom = null
        for (fragment in supportFragmentManager.fragments) {
            if (fragment !is SupportMapFragment) {
                supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
        }
        scheduleGotoDriverSuggestFragment()
    }

    private fun scheduleGotoDriverSuggestFragment() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!mainViewModel.isShowingLayoutBill.get()!! && !mainViewModel.isShowingLayoutBottom.get()!! && currentFragment == Constants.FRAGMENT_MAP) {
                mainViewModel.isShowingLayoutBottom.set(true)
                mainViewModel.isShowingLayoutBill.set(false)
                mainViewModel.isShowingIconBack.set(false)

                fragmentBottom = DriverSuggestFragment()
                currentFragment = Constants.FRAGMENT_DRIVER_SUGGEST
                val transaction = supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_top,
                        R.anim.pop_in_bottom,
                        R.anim.pop_out_top
                )
                transaction.addToBackStack(null)
                transaction.replace(R.id.fragmentBottom, fragmentBottom as DriverSuggestFragment).commitAllowingStateLoss()
                updateSizeFragmentBook()
            }
        }, 1000L)
    }

    private fun gotoInfoDriverFragment() {
        mainViewModel.isShowingLayoutBottom.set(true)
        mainViewModel.isShowingLayoutBill.set(false)
        mainViewModel.isShowingIconBack.set(true)
        mainViewModel.isShowingProgress.set(false)

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
        transaction.replace(R.id.fragmentBottom, fragmentBottom as InfoDriverFragment).commitAllowingStateLoss()
        updateSizeFragmentBook()

    }

    private fun gotoFindPlaceFragment() {
        mainViewModel.isShowingLayoutBottom.set(true)
        mainViewModel.isShowingLayoutBill.set(false)
        mainViewModel.isShowingIconBack.set(true)

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
        transaction.add(R.id.fragmentBottom, fragmentBottom as FindPlaceFragment).commitAllowingStateLoss()

        updateSizeFragmentBook()
    }

    private fun gotoWaitDriverFragment() {
        mainViewModel.isShowingLayoutBottom.set(true)
        mainViewModel.isShowingLayoutBill.set(false)
        mainViewModel.isShowingIconBack.set(true)
        clearMarkerDriver()
        showingOtherDriver = false
        driverManager.getInfoDriverChoosing(mainViewModel.bookInfo.get()!!.driverInfo!!)

        fragmentBottom = WaitDriverFragment()
        currentFragment = Constants.FRAGMENT_WAIT_DRIVER
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.replace(R.id.fragmentBottom, fragmentBottom as WaitDriverFragment).commitAllowingStateLoss()
        updateSizeFragmentBook()
    }

    fun gotoDriverGoingFragment(statusDriverGoingFragment: Int, jsonData: JSONObject) {
        mainViewModel.isShowingLayoutBottom.set(true)
        mainViewModel.isShowingLayoutBill.set(false)
        mainViewModel.isShowingIconBack.set(true)

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
        transaction.replace(R.id.fragmentBottom, fragmentBottom as DriverGoingFragment).commitAllowingStateLoss()
        updateSizeFragmentBook()
    }

    private fun gotoBillFragment() {
        mainViewModel.isShowingLayoutBottom.set(false)
        mainViewModel.isShowingLayoutBill.set(true)
        mainViewModel.isShowingIconBack.set(false)

        fragmentBottom = BillFragment()
        currentFragment = Constants.FRAGMENT_BILL

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_top,
            R.anim.pop_in_bottom,
            R.anim.pop_out_top
        )
        transaction.replace(R.id.fragmentBill, fragmentBottom as BillFragment).commitAllowingStateLoss()
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
            FirebaseConnection.getInstance().pushNotifyToCancelBook(mainViewModel.bookInfo.get()!!) {
                dialogConfirm.dismiss()
                gotoMapFragment()
            }
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

    private fun saveBookInfo(jsonData: JSONObject) {
        val appPreferences = AppPreferences.getInstance(this)
        appPreferences.bookInfoPreferences = mainViewModel.bookInfo.get()
        appPreferences.jsonDataBookInfo = jsonData
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private var mainMap: GoogleMap? = null
        private var driverHashMap: HashMap<String, Marker> = HashMap()
        var currentFragment = Constants.FRAGMENT_MAP
        var showingOtherDriver = true

        fun addOrUpdateMarkerDriver(driverInfo: DriverInfo, typeDriverChooser: TypeDriverValue, idDriverChooser: String?) {
            if (!showingOtherDriver && driverInfo.driverId != idDriverChooser) {
                return
            }
            if (driverHashMap.containsKey(driverInfo.driverId)) {
                // Update marker
                val marker = driverHashMap[driverInfo.driverId]
                marker?.position = LatLng(driverInfo.latitude, driverInfo.longitude)
                Log.d("NamTV", "Update marker")
            } else {
                // Add new marker
                val markerOption = MarkerOptions().apply {
                    position(LatLng(driverInfo.latitude, driverInfo.longitude))
                    icon(MapsUtils.bitmapFromVector(if (typeDriverChooser == TypeDriverValue.GRAB_BIKE) R.drawable.motocross else R.drawable.car))
                    title(driverInfo.name)
                    snippet(driverInfo.rate.toString())
                }
                val marker = mainMap!!.addMarker(markerOption)
                marker.tag = driverInfo.driverId
                driverHashMap[driverInfo.driverId] = marker
                Log.d("NamTV", "Add new marker")
            }

        }

        fun clearMarkerDriver() {
            if (showingOtherDriver) {
                mainMap?.clear()
                driverHashMap.clear()
            }
        }

        fun removeMarkerDriver(driverId: String) {
            val marker = driverHashMap[driverId]
            marker?.remove()
        }
    }
}