package com.example.grabapplication.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.grabapplication.R
import com.example.grabapplication.adapters.DriverSuggestAdapter
import com.example.grabapplication.common.DriverManager
import com.example.grabapplication.databinding.FragmentDriverSuggestBinding
import com.example.grabapplication.model.DriverInfo
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class DriverSuggestFragment : Fragment() {

    private val suggestDriverViewModel: MainViewModel
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
        binding.viewModel = suggestDriverViewModel
        binding.adapter = driverSuggestAdapter
        listDriverSuggest.observe(viewLifecycleOwner, Observer {
//            Log.d("NamTV", "list = ${it.size}")
            it.let (driverSuggestAdapter::submitList)
        })
    }

    private fun setEventView() {
        driverSuggestAdapter.callback = object : DriverSuggestAdapter.OnClickItemDriverListener {
            override fun clickItemDriver(driverInfo: DriverInfo) {
//                MapsConnection.getInstance().getShortestWay(placeModel.lat, placeModel.lng) {
//                    it.endAddress = placeModel.formattedAddress
//                    updateDistancePlaceChoose(it)
//                    showListPlace(false)
//                }

            }
        }
    }

    private fun getListDriverSuggest() {
        val listDriver = DriverManager.getInstance().listDriverHashMap.values
        for (driverInfo in listDriver) {
            listDriverSuggest.value?.add(driverInfo)
            if (listDriverSuggest.value!!.size >= 5) {
                break
            }
        }
    }
}