package com.example.grabapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.databinding.FragmentInputPasswordBinding
import com.example.grabapplication.databinding.FragmentInputPhoneNumberBinding
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

        return view
    }
}