package com.example.grabapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.databinding.FragmentBillBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class BillFragment : Fragment() {
    private val billViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(MainViewModel::class.java)
            }

    private lateinit var binding: FragmentBillBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill, container, false)
        val view = binding.root
        binding.viewModel = billViewModel
        setupEvent()
        return view
    }

    private fun setupEvent() {
        billViewModel.onItemClickListener = object : MainViewModel.OnItemClickListener {
            override fun openFindPlaceFragment() {
                Log.d("NamTV", "test")
                // Do nothing
            }

            override fun bookDriver() {
                // Do nothing
            }

            override fun endBook() {
                val rating = binding.rating.rating.toInt()
                if (rating > 0) {
                    HttpConnection.getInstance().voteStarDriver(billViewModel.bookInfo.get()!!.driverInfo!!.driverId, rating)
                }
                if (activity is MainActivity) {
                    (activity as MainActivity).gotoMapFragment()
                }
            }
        }
    }
}