package com.example.grabapplication.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.databinding.FragmentWaitDriverBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class DriverGoingFragment : Fragment() {

    private val waitDriverViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(
                        MainViewModel::class.java)
            }

    private lateinit var binding: FragmentWaitDriverBinding
    private var countDownTimer: CountDownTimer? = null
    private var isDialogShowing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_going, container, false)
        val view = binding.root
        binding.viewModel = waitDriverViewModel
        return view
    }
}