package com.example.grabapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.databinding.FragmentInfoDriverBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class InfoDriverFragment : Fragment() {

    private val infoDriverViewModel: MainViewModel
        by lazy {
            ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(MainViewModel::class.java)
        }

    private lateinit var binding: FragmentInfoDriverBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info_driver, container, false)
        val view = binding.root
        binding.viewModel = infoDriverViewModel
        return view
    }
}