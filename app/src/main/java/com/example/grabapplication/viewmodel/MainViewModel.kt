package com.example.grabapplication.viewmodel

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grabapplication.common.Constants
import com.example.grabapplication.googlemaps.MapsConnection
import com.example.grabapplication.googlemaps.MapsConstant
import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.googlemaps.models.PlaceModel
import com.example.grabapplication.model.DriverInfo

class MainViewModel: ViewModel() {
    var isShowMapLayout = ObservableField(true)
    var isShowingListPlace = ObservableField(true)
    var driverInfoSelect: DriverInfo? = null
    var distanceDriver: Distance? = null
    var listPlace = MutableLiveData<ArrayList<PlaceModel>>()

    var onItemClickListener: OnItemClickListener? = null
    var distancePlaceChoose: ObservableField<Distance> = ObservableField(MapsConstant.DEFAULT_DISTANCE)

    fun openFindPlaceFragment() {
        onItemClickListener?.openFindPlaceFragment()
    }

    fun selectDriver(driverInfo: DriverInfo, callback: (Boolean) -> Unit) {
        driverInfoSelect = driverInfo
        MapsConnection.getInstance().getShortestWay(driverInfo.latitude, driverInfo.longitude) {
            distanceDriver = it
            callback.invoke(true)
        }
    }

    fun bookDriver() {
        onItemClickListener?.bookDriver()
    }

    interface OnItemClickListener {
        fun openFindPlaceFragment()
        fun bookDriver()
    }
}

