package com.example.grabapplication.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.grabapplication.R
import com.example.grabapplication.adapters.ListPlaceAdapter
import com.example.grabapplication.common.*
import com.example.grabapplication.databinding.FragmentFindPlaceBinding
import com.example.grabapplication.googlemaps.MapsConnection
import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.googlemaps.models.PlaceModel
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class FindPlaceFragment : Fragment() {

    private val findPlaceViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(MainViewModel::class.java)
            }

    private lateinit var binding: FragmentFindPlaceBinding
    private var lastPlace = ""
    private val listPlaceAdapter by lazy {
        ListPlaceAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_place, container, false)
        val view = binding.root

        initView()
        setEventView()
        return view
    }

    private fun initView() {
        binding.viewModel = findPlaceViewModel
        binding.adapter = listPlaceAdapter
        findPlaceViewModel.listPlace.value?.clear()
        findPlaceViewModel.listPlace.observe(this, Observer {
            Log.d("NamTV", "list = ${it.size}")
            it.let (listPlaceAdapter::submitList)
        })

        showListPlace(true)
    }

    private fun setEventView() {
        binding.edtEndAddress.apply {
            afterTextChanged {
                if (it != lastPlace && !findPlaceViewModel.listPlace.value.isNullOrEmpty()) {
                    findPlaceViewModel.listPlace.value?.clear()
                    showListPlace(true)
                }
            }

            onFocusChanged {
                findPlaceFromStringEditText(it)
            }

            onEditorActionDone {
                findPlaceFromStringEditText(it)
                CommonUtils.clearFocusEditText(requireActivity())
            }
        }


        binding.layoutView.setOnSingleClickListener(View.OnClickListener {
            CommonUtils.clearFocusEditText(requireActivity())
        })

        listPlaceAdapter.callback = object : ListPlaceAdapter.OnClickItemPlaceListener {
            override fun clickItemPlace(placeModel: PlaceModel) {
                lastPlace = placeModel.name
                binding.edtEndAddress.setText(placeModel.name)
                CommonUtils.clearFocusEditText(requireActivity())

                MapsConnection.getInstance().getShortestWay(placeModel.lat, placeModel.lng) {
                    it.endAddress = placeModel.formattedAddress
                    updateDistancePlaceChoose(it)
                    showListPlace(false)
                }
            }
        }
    }

    private fun findPlaceFromStringEditText(edtString: String) {
        if (edtString.isNotEmpty()) {
            Log.d("NamTV", "findPlaceFromStringEditText: $edtString")
            if (lastPlace != edtString) {
                lastPlace = edtString
                MapsConnection.getInstance().findPlace(edtString) {
                    findPlaceViewModel.listPlace.value = it
                }
            }
        }
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

    private fun showListPlace(show: Boolean) {
        findPlaceViewModel.isShowingListPlace.set(show)
    }

    private fun getPrice(distanceValue: Int): String {
        val priceOfKilometer = AppPreferences.getInstance(requireContext()).priceOfKilometer
        val price = distanceValue / 1000 * priceOfKilometer

        return getString(R.string.price, price.toString())
    }
}