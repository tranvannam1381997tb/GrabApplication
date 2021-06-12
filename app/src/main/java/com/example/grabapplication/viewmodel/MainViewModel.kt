package com.example.grabapplication.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.example.grabapplication.manager.AccountManager
import com.example.grabapplication.manager.DriverManager
import com.example.grabapplication.googlemaps.MapsConnection
import com.example.grabapplication.googlemaps.MapsConstant
import com.example.grabapplication.googlemaps.models.Distance
import com.example.grabapplication.model.BookInfo
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.TypeDriverValue

class MainViewModel: ViewModel() {
    var isShowingLayoutBottom = ObservableField(false)
    var isShowingLayoutBill = ObservableField(false)
    var isShowingListDriverSuggest = ObservableField(false)
    var isShowingIconBack = ObservableField(false)
    var isShowingProgress = ObservableField(false)
    var isChoosingGrabBike = ObservableField(true)

    val accountManager = AccountManager.getInstance()

    var distanceDriver: Distance? = null

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

    fun chooseGrabBike() {
        isChoosingGrabBike.set(true)
        DriverManager.getInstance().changeTypeDriverChoosing(TypeDriverValue.GRAB_BIKE)
    }

    fun chooseGrabCar() {
        isChoosingGrabBike.set(false)
        DriverManager.getInstance().changeTypeDriverChoosing(TypeDriverValue.GRAB_CAR)
    }

    interface OnItemClickListener {
        fun openFindPlaceFragment()
        fun bookDriver()
        fun endBook()
        fun clickIconPhone()
    }

    interface OnClickDriverSuggest {
        fun clickTxtSuggestDriver()
    }
}

