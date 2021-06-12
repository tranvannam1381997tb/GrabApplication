package com.example.grabapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.*
import com.example.grabapplication.customviews.CustomAutoCompletedSearch
import com.example.grabapplication.databinding.FragmentFindPlaceBinding
import com.example.grabapplication.googlemaps.MapsConnection
import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.manager.AccountManager
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlin.math.cos

private const val DEFAULT_COUNTRY = "vn"
private const val DEFAULT_BOUND = 15000
class FindPlaceFragment : Fragment() {

    private val findPlaceViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(MainViewModel::class.java)
            }

    private lateinit var binding: FragmentFindPlaceBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_place, container, false)
        val view = binding.root
        binding.viewModel = findPlaceViewModel

        setEventView()
        return view
    }

    private fun setEventView() {
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as CustomAutoCompletedSearch

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setCountries(DEFAULT_COUNTRY)
        autocompleteFragment.setLocationBias(setBounds(DEFAULT_BOUND))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("NamTV", "Place: " + place.name.toString() + ", " + place.id)
                val latLng = place.latLng
                if (latLng != null) {
                    MapsConnection.getInstance().getShortestWay(latLng.latitude, latLng.longitude) {
                        it.endAddress = place.address!!
                        updateDistancePlaceChoose(it)
                        updateLayoutDistanceInfo()
                        binding.layoutBookDriver.visibility = View.VISIBLE
                    }
                }
            }

            override fun onError(status: Status) {
                Log.i("NamTV", "An error occurred: $status")
            }
        })

        autocompleteFragment.btnClear.setOnClickListener {
            autocompleteFragment.editText.text = null
            autocompleteFragment.btnClear.visibility = View.INVISIBLE
            binding.layoutBookDriver.visibility = View.INVISIBLE
        }
    }

    private fun setBounds(mDistanceInMeters: Int): RectangularBounds {
        val currentLocation = AccountManager.getInstance().getLocationUser()
        val latRadian = Math.toRadians(currentLocation.latitude)
        val degLatKm = 110.574235
        val degLongKm = 110.572833 * cos(latRadian)
        val deltaLat = mDistanceInMeters / 1000.0 / degLatKm
        val deltaLong = mDistanceInMeters / 1000.0 / degLongKm
        val minLat = currentLocation.latitude - deltaLat
        val minLong = currentLocation.longitude - deltaLong
        val maxLat = currentLocation.latitude + deltaLat
        val maxLong = currentLocation.longitude + deltaLong
        return RectangularBounds.newInstance(LatLng(minLat, minLong), LatLng(maxLat, maxLong))
    }

    private fun updateDistancePlaceChoose(distance: Distance) {
        findPlaceViewModel.bookInfo.get()!!.startAddress = distance.startAddress
        findPlaceViewModel.bookInfo.get()!!.endAddress = distance.endAddress
        findPlaceViewModel.bookInfo.get()!!.latStart = distance.latStart
        findPlaceViewModel.bookInfo.get()!!.lngStart = distance.lngStart
        findPlaceViewModel.bookInfo.get()!!.latEnd = distance.latEnd
        findPlaceViewModel.bookInfo.get()!!.lngEnd = distance.lngEnd
        findPlaceViewModel.bookInfo.get()!!.distance = distance.distanceText
        findPlaceViewModel.bookInfo.get()!!.duration = distance.durationText
        findPlaceViewModel.bookInfo.get()!!.price = getPrice(distance.distanceValue)
    }

    private fun updateLayoutDistanceInfo() {
        binding.txtDistance.text = getString(R.string.distance_length, findPlaceViewModel.bookInfo.get()!!.distance)
        binding.txtDuration.text = getString(R.string.time_to_move, findPlaceViewModel.bookInfo.get()!!.duration)
        binding.txtPrice.text = getString(R.string.notify_price, findPlaceViewModel.bookInfo.get()!!.price)
    }

    private fun getPrice(distanceValue: Int): String {
        val priceOfKilometer = AppPreferences.getInstance(requireContext()).priceOfKilometer
        val price = distanceValue / 1000 * priceOfKilometer

        return getString(R.string.price, price.toString())
    }
}