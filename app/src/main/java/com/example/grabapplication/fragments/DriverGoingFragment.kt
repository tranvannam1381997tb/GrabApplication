package com.example.grabapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.databinding.FragmentDriverGoingBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class DriverGoingFragment : Fragment() {

    private val waitDriverViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(
                        MainViewModel::class.java)
            }

    private lateinit var binding: FragmentDriverGoingBinding
    private var currentStatus = STATUS_ARRIVING_ORIGIN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_going, container, false)
        val view = binding.root
        binding.viewModel = waitDriverViewModel
        updateLayout()
        setupEvent()
        return view
    }

    private fun setupEvent() {
        binding.btnCancel.setOnSingleClickListener(View.OnClickListener {
            when (currentStatus) {
                STATUS_ARRIVING_ORIGIN -> handleClickBtnCancel()
            }
        })
    }

    private fun updateLayout() {
        val bundle = arguments
        if (bundle != null) {
            currentStatus = bundle.getInt(STATUS_DRIVER_GOING_FRAGMENT)
            when (currentStatus) {
                STATUS_ARRIVING_ORIGIN -> updateLayoutArrivingOrigin()

                STATUS_ARRIVED_ORIGIN -> updateLayoutArrivedOrigin()

//                STATUS_GOING -> updateLayoutGoing()
//
//                STATUS_ARRIVED_DESTINATION -> updateLayoutArrivedDestination()
            }
        }
    }

    private fun updateLayoutArrivingOrigin() {
        binding.description.setText(R.string.driver_arriving_origin)
        binding.btnCancel.visibility = View.VISIBLE
        binding.btnCancel.setText(R.string.cancel_book)
//        binding.txtNotify.setText()
    }

    private fun updateLayoutArrivedOrigin() {
        binding.description.setText(R.string.driver_arrived_origin)
        binding.btnCancel.visibility = View.GONE
    }

    private fun handleClickBtnCancel() {
        if (activity is MainActivity) {
            (activity as MainActivity).showDialogConfirmCancelBook()
        }
    }

    companion object {
        const val STATUS_DRIVER_GOING_FRAGMENT = "statusDriverGoingFragment"
        const val STATUS_ARRIVING_ORIGIN = 0
        const val STATUS_ARRIVED_ORIGIN = 1
        const val STATUS_GOING = 2
        const val STATUS_ARRIVED_DESTINATION = 3

    }
}