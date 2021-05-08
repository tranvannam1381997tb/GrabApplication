package com.example.grabapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.activities.SignUpActivity
import com.example.grabapplication.common.*
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.databinding.FragmentInputInfoBinding
import com.example.grabapplication.model.UserInfoKey
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_input_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class InputInfoFragment : Fragment() {

    private val inputInfoViewModel: SignUpViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(
                    SignUpViewModel::class.java)
            }

    private lateinit var binding: FragmentInputInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_input_info, container, false)
        val view = binding.root
        binding.viewModel = inputInfoViewModel
        setEventListener()
        return view
    }

    private fun setEventListener() {
        binding.btnNextInputInfo.setOnSingleClickListener(View.OnClickListener {
            if (validateInfo()) {
                inputInfoViewModel.name = binding.edtName.text.toString()
                inputInfoViewModel.age = binding.edtAge.text.toString().toInt()
                inputInfoViewModel.sex = if (inputInfoViewModel.isCheckMale.get()!!) SexValue.MALE.rawValue else SexValue.FEMALE.rawValue
                HttpConnection.getInstance().startSignUp(getJSONInfo()) { isSuccess, dataResponse ->
                    if (isSuccess) {
                        val jsonObject = JSONObject(dataResponse)
                        val userId = CommonUtils.getStringFromJsonObject(jsonObject, UserInfoKey.KeyUserId.rawValue)
                        val accountManager = AccountManager.getInstance()
                        accountManager.saveIdUser(userId)
                        startMainActivity()
                    } else {
                        showToastError(dataResponse)
                    }
                }
            }
        })
    }

    private fun getJSONInfo(): JSONObject {
        val jsonInfo = JSONObject()
        if (inputInfoViewModel.phoneNumber!!.startsWith("0")) {
            val phoneNumber = "+84" + inputInfoViewModel.phoneNumber!!.substring(1, inputInfoViewModel.phoneNumber!!.length)
            jsonInfo.put(UserInfoKey.KeyPhoneNumber.rawValue, phoneNumber)
        } else {
            jsonInfo.put(UserInfoKey.KeyPhoneNumber.rawValue, inputInfoViewModel.phoneNumber)
        }
        jsonInfo.put(UserInfoKey.KeyPassword.rawValue, inputInfoViewModel.password)
        jsonInfo.put(UserInfoKey.KeyName.rawValue, inputInfoViewModel.name)
        jsonInfo.put(UserInfoKey.KeyAge.rawValue, inputInfoViewModel.age)
        jsonInfo.put(UserInfoKey.KeySex.rawValue, inputInfoViewModel.sex)
        return jsonInfo
    }

    private fun validateInfo(): Boolean {
        if (binding.edtName.text.isNullOrEmpty()) {
            showToastError(getString(R.string.error_input_name))
            return false
        }
        if (binding.edtAge.text.isNullOrEmpty()) {
            showToastError(getString(R.string.error_input_age))
            return false
        }
        return true
    }

    private fun showToastError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }

    private fun startMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        context?.startActivity(intent)
    }
}