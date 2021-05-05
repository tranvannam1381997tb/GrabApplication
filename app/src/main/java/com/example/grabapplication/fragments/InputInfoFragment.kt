package com.example.grabapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.databinding.FragmentInputInfoBinding
import com.example.grabapplication.databinding.FragmentInputPhoneNumberBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_input_info.*

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
                // TODO requet server
            }
        })
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
}