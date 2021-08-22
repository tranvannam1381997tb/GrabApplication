package com.example.grabapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.databinding.FragmentInputPasswordBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.SignUpViewModel

class InputPasswordFragment : Fragment() {

    private val inputPasswordViewModel: SignUpViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(
                    SignUpViewModel::class.java)
            }

    private lateinit var binding: FragmentInputPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_input_password, container, false)
        val view = binding.root
        binding.viewModel = inputPasswordViewModel
        setUpEvent()
        return view
    }

    private fun setUpEvent(){
        binding.btnNextInputPassword.setOnSingleClickListener(View.OnClickListener {
            if (validatePassword(binding.edtPassword.text.toString())) {
                inputPasswordViewModel.password = binding.edtPassword.text.toString()
                inputPasswordViewModel.onClickSignUpScreenListener?.clickBtnNextInputPassword()
            } else {
                showToastError(getString(R.string.error_password_invalid))
            }
        })
    }

    private fun validatePassword(password: String): Boolean {
        if (password.isNotEmpty()) {
            return true
        }
        return false
    }

    private fun showToastError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }
}