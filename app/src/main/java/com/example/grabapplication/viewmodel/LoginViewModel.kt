package com.example.grabapplication.viewmodel

import android.content.Context
import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.example.grabapplication.activities.MainActivity

class LoginViewModel(var context: Context): ViewModel() {

    var isEnableBtnLogin: ObservableField<Boolean> = ObservableField(false)

    fun validateDataLogin(username: String?, password: String?) {
        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            isEnableBtnLogin.set(true)
            return
        }
        isEnableBtnLogin.set(false)
    }

    fun startLogin(username: String, password: String) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)

    }
}