package com.example.grabapplication.viewmodel

import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel() {
    var onClickSignUpScreenListener: OnClickSignUpScreenListener? = null

    fun clickBtnNextInputPhoneNumber() {
        onClickSignUpScreenListener?.clickBtnNextInputPhoneNumber()
    }

    fun clickBtnNextInputPassword() {
        onClickSignUpScreenListener?.clickBtnNextInputPassword()
    }

}

interface OnClickSignUpScreenListener {
    fun clickBtnNextInputPhoneNumber()

    fun clickBtnNextInputPassword()
}