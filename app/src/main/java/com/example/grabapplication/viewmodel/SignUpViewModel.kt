package com.example.grabapplication.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel() {

    var isCheckMale: ObservableField<Boolean> = ObservableField(true)

    var onClickSignUpScreenListener: OnClickSignUpScreenListener? = null

    fun clickBtnNextInputPhoneNumber() {
        onClickSignUpScreenListener?.clickBtnNextInputPhoneNumber()
    }

    fun clickBtnNextInputPassword() {
        onClickSignUpScreenListener?.clickBtnNextInputPassword()
    }

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