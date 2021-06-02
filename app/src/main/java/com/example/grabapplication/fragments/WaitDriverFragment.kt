package com.example.grabapplication.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.activities.MainActivity
import com.example.grabapplication.common.Constants
import com.example.grabapplication.common.setOnSingleClickListener
import com.example.grabapplication.customviews.ConfirmDialog
import com.example.grabapplication.databinding.FragmentWaitDriverBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.MainViewModel

class WaitDriverFragment : Fragment() {

    private val waitDriverViewModel: MainViewModel
            by lazy {
                ViewModelProvider(requireActivity(), BaseViewModelFactory(requireContext())).get(
                    MainViewModel::class.java)
            }

    private lateinit var binding: FragmentWaitDriverBinding
    var countDownTimer: CountDownTimer? = null
    private var dialog: ConfirmDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wait_driver, container, false)
        val view = binding.root
        binding.viewModel = waitDriverViewModel
        setupEvent()
        return view
    }

    override fun onResume() {
        super.onResume()
        setupCountDownTimer()
    }

    private fun setupEvent() {
        binding.btnCancel.setOnSingleClickListener(View.OnClickListener {
            showDialogConfirmCancelBook()
        })
    }

    private fun setupCountDownTimer() {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(Constants.TIME_WAIT_DRIVER, Constants.COUNT_DOWN_INTERVAL) {
                override fun onFinish() {
                    if (activity is MainActivity) {
                        showDialogBookNew()
                        Log.d("NamTV", "onFinish")
                    }
                }

                override fun onTick(millisUntilFinished: Long) {
                    waitDriverViewModel.countDownTimer.set((millisUntilFinished/1000).toInt())
                }
            }
            countDownTimer!!.start()
        }
    }

    fun showDialogConfirmCancelBook() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        dialog = ConfirmDialog(requireContext())
        dialog!!.setTextDisplay(
            getString(R.string.confirm_cancel_book),
            null,
            getString(R.string.no),
            getString(R.string.cancel_book)
        )
        dialog!!.setOnClickOK(View.OnClickListener {
            dialog!!.dismiss()
            gotoMapFragment()

            // TODO
        })
        dialog!!.setOnClickCancel(View.OnClickListener {
            dialog!!.dismiss()
        })
        dialog!!.setTextTypeBoldBtnOK()
        dialog!!.show()
    }

    fun gotoMapFragment() {
        countDownTimer?.cancel()
        if (activity is MainActivity) {
            (activity as MainActivity).gotoMapFragment()
        }
    }

    fun showDialogBookNew() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        dialog = ConfirmDialog(requireContext())
        dialog!!.setTextDisplay(
            getString(R.string.driver_cancel),
            null,
            null,
            getString(R.string.label_ok)
        )
        dialog!!.setOnClickOK(View.OnClickListener {
            dialog!!.dismiss()
            gotoMapFragment()
            // TODO
        })

        dialog!!.setTextTypeBoldBtnOK()
        dialog!!.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }
}