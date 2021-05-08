package com.example.grabapplication.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.grabapplication.model.UserInfo
import com.google.android.gms.maps.model.LatLng

class SignUpViewModel: ViewModel() {

    var name: String? = null
    var password: String? = null
    var age: Int? = null
    var sex: Int? = null
    var phoneNumber: String? = null

    var isCheckMale: ObservableField<Boolean> = ObservableField(true)

    var onClickSignUpScreenListener: OnClickSignUpScreenListener? = null
    
    fun checkRadioMale() {
        isCheckMale.set(true)
    }

    fun checkRadioFemale() {
        isCheckMale.set(false)
    }
}

interface OnClickSignUpScreenListener {
    fun clickBtnNextInputPhoneNumber()

    fun clickBtnNextInputPassword()
}