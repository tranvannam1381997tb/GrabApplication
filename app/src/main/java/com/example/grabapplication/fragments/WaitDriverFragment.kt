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
import com.example.grabapplication.databinding.FragmentInfoDriverBinding
import com.example.grabapplication.databinding.FragmentWaitDriverBinding
import com.example.grabapplication.firebase.FirebaseConnection
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
    private var isDialogShowing = false

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
        val dialogConfirm = ConfirmDialog(requireContext())
        dialogConfirm.setTextDisplay(
            getString(R.string.confirm_cancel_book),
            null,
            getString(R.string.no),
            getString(R.string.cancel_book)
        )
        dialogConfirm.setOnClickOK(View.OnClickListener {
            isDialogShowing = false
            dialogConfirm.dismiss()
            gotoMapFragment()

            // TODO
        })
        dialogConfirm.setOnClickCancel(View.OnClickListener {
            isDialogShowing = false
            dialogConfirm.dismiss()
        })
        dialogConfirm.setTextTypeBoldBtnOK()
        if (!isDialogShowing) {
            dialogConfirm.show()
            isDialogShowing = true
        }
    }

    fun gotoMapFragment() {
        countDownTimer?.cancel()
        if (activity is MainActivity) {
            (activity as MainActivity).gotoMapFragment()
        }
    }

    fun showDialogBookNew() {
        val dialogConfirm = ConfirmDialog(requireContext())
        dialogConfirm.setTextDisplay(
            getString(R.string.driver_cancel),
            null,
            null,
            getString(R.string.label_ok)
        )
        dialogConfirm.setOnClickOK(View.OnClickListener {
            isDialogShowing = false
            dialogConfirm.dismiss()
            gotoMapFragment()
            // TODO
        })

        dialogConfirm.setTextTypeBoldBtnOK()
        if (isDialogShowing) {
            dialogConfirm.show()
            isDialogShowing = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}