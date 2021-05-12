package com.example.grabapplication.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.*
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.customviews.ConfirmDialog
import com.example.grabapplication.databinding.ActivityLoginBinding
import com.example.grabapplication.model.UserInfoKey
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.LoginViewModel
import kotlinx.coroutines.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel
        by lazy {
            ViewModelProvider(this, BaseViewModelFactory(this)).get(LoginViewModel::class.java)
        }

    private lateinit var binding: ActivityLoginBinding

    private var jobStartLogin: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = loginViewModel
        setEventView()
    }

    private fun setEventView() {
        binding.edtPhoneNumber.apply {
            afterTextChanged {
                loginViewModel.validateDataLogin(
                    binding.edtPhoneNumber.text.toString(),
                    binding.edtPassword.text.toString()
                )
            }

            onEditorActionNext {
                binding.edtPassword.requestFocus()
            }
        }

        binding.edtPassword.apply {
            afterTextChanged {
                loginViewModel.validateDataLogin(
                    binding.edtPhoneNumber.text.toString(),
                    binding.edtPassword.text.toString()
                )
            }

            onEditorActionDone {
                if (loginViewModel.isEnableBtnLogin.get()!!) {
                    startLogin()
                }
            }
        }

        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            startLogin()
        }

        // TODO debug code
        binding.edtPhoneNumber.setText("0976356351")
        binding.edtPassword.setText("123456")
    }

    private fun startLogin() {
        HttpConnection.getInstance().startLogin(getJSONLogin()) { isSuccess, dataResponse ->
            if (isSuccess) {
                val jsonObject = JSONObject(dataResponse)
                if (saveUserInfo(jsonObject)) {
                    startMainActivity()
                } else {
                    showDialogError(getString(R.string.login_false))
                }
            } else {
                showDialogError(dataResponse)
            }
        }
    }

    private fun getJSONLogin(): JSONObject {
        val jsonBody = JSONObject()
        val phoneNumber = binding.edtPhoneNumber.text.toString()
        if (phoneNumber.startsWith("0")) {
            val phoneNumberFormat = "+84" + phoneNumber.substring(1, phoneNumber.length)
            jsonBody.put(UserInfoKey.KeyPhoneNumber.rawValue, phoneNumberFormat)
        } else {
            jsonBody.put(UserInfoKey.KeyPhoneNumber.rawValue, phoneNumber)
        }
        jsonBody.put(UserInfoKey.KeyPassword.rawValue, binding.edtPassword.text.toString())
        return jsonBody
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun saveUserInfo(jsonObject: JSONObject): Boolean {
        val userInfo = CommonUtils.getJsonObjectFromJsonObject(jsonObject, UserInfoKey.KeyUser.rawValue)

        val userId = CommonUtils.getStringFromJsonObject(userInfo, UserInfoKey.KeyUserId.rawValue)
        if (userId.isEmpty()) {
            return false
        }
        val accountManager = AccountManager.getInstance()
        accountManager.saveIdUser(userId)

        val name = CommonUtils.getStringFromJsonObject(userInfo, UserInfoKey.KeyName.rawValue)
        val age = CommonUtils.getIntFromJsonObject(userInfo, UserInfoKey.KeyAge.rawValue)
        val sex = CommonUtils.getIntFromJsonObject(userInfo, UserInfoKey.KeySex.rawValue)
        val phoneNumber = CommonUtils.getStringFromJsonObject(userInfo, UserInfoKey.KeyPhoneNumber.rawValue)
        val status = CommonUtils.getIntFromJsonObject(userInfo, UserInfoKey.KeyStatus.rawValue)
        accountManager.setUserInfo(name, age, sex, phoneNumber, status)
        return true
    }

    private fun showDialogError(message: String?) {
        if (!message.isNullOrEmpty()) {
            val dialogError = ConfirmDialog(this)
            dialogError.setTextDisplay(getString(R.string.error), message, null, getString(R.string.label_ok))
            dialogError.setOnClickOK(View.OnClickListener {
                dialogError.dismiss()
            })
            dialogError.setHideLineButton()
            dialogError.show()
        }
    }
}