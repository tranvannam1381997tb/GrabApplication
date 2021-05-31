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
import com.example.grabapplication.common.CommonUtils
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.databinding.FragmentDriverGoingBinding
import com.example.grabapplication.firebase.FirebaseConstants
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel
import org.json.JSONObject
import java.util.*

class DriverGoingFragment : Fragment() {

    private val driverGoingViewModel: MainViewModel
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
        binding.viewModel = driverGoingViewModel
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
            val jsonData = JSONObject(bundle.getString(JSON_DATA, "{}"))
            when (currentStatus) {
                STATUS_ARRIVING_ORIGIN -> updateLayoutArrivingOrigin(jsonData.getInt(FirebaseConstants.KEY_TIME_ARRIVED_ORIGIN))

                STATUS_ARRIVED_ORIGIN -> updateLayoutArrivedOrigin(jsonData.getString(FirebaseConstants.KEY_START_ADDRESS))

                STATUS_ARRIVING_DESTINATION -> updateLayoutGoing(bundle.getInt(FirebaseConstants.KEY_TIME_ARRIVED_DESTINATION))

                STATUS_ARRIVED_DESTINATION -> updateLayoutArrivedDestination()
            }
        }
    }

    private fun updateLayoutArrivingOrigin(timeArrivedOrigin: Int) {
        binding.description.setText(R.string.driver_arriving_origin)
        binding.btnCancel.visibility = View.VISIBLE
        binding.btnCancel.setText(R.string.cancel_book)
        val currentTime = Calendar.getInstance()
        currentTime.add(Calendar.SECOND, timeArrivedOrigin)
        val timeDriverArrived = CommonUtils.getTimeArrived(currentTime)
        val strNotify = getString(R.string.notify_time_driver_arrived_origin, timeDriverArrived)
        binding.txtNotify.text = strNotify
        binding.txtNotify.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.VISIBLE
    }

    private fun updateLayoutArrivedOrigin(startAddress: String) {
        binding.description.setText(R.string.driver_arrived_origin)
        val strNotify = getString(R.string.notify_start_address, startAddress)
        binding.txtNotify.text = strNotify
        binding.txtNotify.visibility = View.VISIBLE
    }


    private fun updateLayoutGoing(timeDriverArrivedDestination: Int) {
        binding.description.setText(R.string.driver_start_going)
        binding.btnCancel.visibility = View.GONE
        val currentTime = Calendar.getInstance()
        currentTime.add(Calendar.SECOND, timeDriverArrivedDestination)
        val timeDriverArrived = CommonUtils.getTimeArrived(currentTime)
        val timeDriverArrivedDestination = getString(R.string.notify_time_driver_arrived_destination, timeDriverArrived)
        val endAddress = getString(R.string.notify_end_address, driverGoingViewModel.bookInfo.get()!!.endAddress)
        binding.txtNotify.text = endAddress
        binding.txtTime.text = timeDriverArrivedDestination
        binding.txtNotify.visibility = View.VISIBLE
        binding.txtTime.visibility = View.VISIBLE
    }

    private fun updateLayoutArrivedDestination() {
        binding.description.setText(R.string.driver_arrived_destination)
        binding.btnCancel.visibility = View.GONE
    }

    private fun handleClickBtnCancel() {
        if (activity is MainActivity) {
            (activity as MainActivity).showDialogConfirmCancelBook()
        }
    }

    companion object {
        const val STATUS_DRIVER_GOING_FRAGMENT = "statusDriverGoingFragment"
        const val JSON_DATA = "jsonData"
        const val STATUS_ARRIVING_ORIGIN = 0
        const val STATUS_ARRIVED_ORIGIN = 1
        const val STATUS_ARRIVING_DESTINATION = 2
        const val STATUS_ARRIVED_DESTINATION = 3
    }
}