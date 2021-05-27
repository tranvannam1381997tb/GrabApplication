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
import com.example.grabapplication.model.BookInfo
import com.example.grabapplication.model.DriverInfo

class MainViewModel: ViewModel() {
    var isShowingListPlace = ObservableField(true)
    var isShowingLayoutBottom = ObservableField(false)
    var isShowingLayoutBill = ObservableField(false)
    var isShowingListDriverSuggest = ObservableField(false)

    var distanceDriver: Distance? = null
    var listPlace = MutableLiveData<ArrayList<PlaceModel>>()

    var onItemClickListener: OnItemClickListener? = null
    var onClickDriverSuggest: OnClickDriverSuggest? = null
    var bookInfo: ObservableField<BookInfo> = ObservableField(MapsConstant.DEFAULT_BOOK_INFO)

    var countDownTimer = ObservableField(60)

    fun selectDriver(driverInfo: DriverInfo, callback: (Boolean) -> Unit) {
        bookInfo.get()!!.driverInfo = driverInfo

        MapsConnection.getInstance().getShortestWay(driverInfo.latitude, driverInfo.longitude) {
            distanceDriver = it
            callback.invoke(true)
        }
    }

    interface OnItemClickListener {
        fun openFindPlaceFragment()
        fun bookDriver()
        fun endBook()
    }

    interface OnClickDriverSuggest {
        fun clickTxtSuggestDriver()
    }
}

