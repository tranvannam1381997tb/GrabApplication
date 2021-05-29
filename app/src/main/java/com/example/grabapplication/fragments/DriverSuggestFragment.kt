package com.example.grabapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.adapters.DriverSuggestAdapter
import com.example.grabapplication.common.DriverManager
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.databinding.FragmentDriverSuggestBinding
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.model.DriverStatus
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel


class DriverSuggestFragment : Fragment() {

    private val driverSuggestViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(MainViewModel::class.java)
            }

    private lateinit var binding: FragmentDriverSuggestBinding
    private val driverSuggestAdapter by lazy {
        DriverSuggestAdapter()
    }

    private val listDriverSuggest = MutableLiveData<ArrayList<DriverInfo>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_suggest, container, false)
        val view = binding.root

        getListDriverSuggest()
        initView()
        setEventView()
        return view
    }

    private fun initView() {
        binding.viewModel = driverSuggestViewModel
        binding.adapter = driverSuggestAdapter
        listDriverSuggest.observe(viewLifecycleOwner, Observer {
            it.let (driverSuggestAdapter::submitList)
        })
        if (driverSuggestViewModel.isShowingListDriverSuggest.get()!!) {
            binding.txtSuggestDriver.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down, 0)
        } else {
            binding.txtSuggestDriver.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up, 0)
        }
    }

    private fun setEventView() {
        driverSuggestAdapter.callback = object : DriverSuggestAdapter.OnClickItemDriverListener {
            override fun clickItemDriver(driverInfo: DriverInfo) {
                if (activity is MainActivity) {
                    val mainActivity = activity as MainActivity
                    if (!mainActivity.isChoosingDriver) {
                        mainActivity.isChoosingDriver = true
                        (activity as MainActivity).selectDriver(driverInfo) {
                            mainActivity.isChoosingDriver = false
                        }
                    }
                }
            }
        }

        driverSuggestViewModel.onClickDriverSuggest = object : MainViewModel.OnClickDriverSuggest {
            override fun clickTxtSuggestDriver() {
                if (driverSuggestViewModel.isShowingListDriverSuggest.get()!!) {
                    driverSuggestViewModel.isShowingListDriverSuggest.set(false)
                    binding.txtSuggestDriver.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up, 0)
                } else {
                    driverSuggestViewModel.isShowingListDriverSuggest.set(true)
                    binding.txtSuggestDriver.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down, 0)
                }
            }
        }

        binding.iconRefresh.setOnSingleClickListener(View.OnClickListener {
            getListDriverSuggest()
        })
    }

    private fun getListDriverSuggest() {
        val mutableDriver = DriverManager.getInstance().listDriverHashMap.values
        val listDriver = ArrayList<DriverInfo>()
        for (driverInfo in mutableDriver) {
            if (driverInfo.status == DriverStatus.StatusOn.rawValue) {
                listDriver.add(driverInfo)
                if (listDriver.size >= 5) {
                    break
                }
            }
        }
        listDriverSuggest.value = listDriver
    }
}