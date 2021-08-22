package com.example.grabapplication.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.databinding.ActivitySignUpBinding
import com.example.grabapplication.fragments.InputInfoFragment
import com.example.grabapplication.fragments.InputPasswordFragment
import com.example.grabapplication.fragments.InputPhoneNumberFragment
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.OnClickSignUpScreenListener
import com.example.grabapplication.viewmodel.SignUpViewModel

class SignUpActivity : AppCompatActivity() {
    private val signUpViewModel: SignUpViewModel
            by lazy {
                ViewModelProvider(this, BaseViewModelFactory(this)).get(SignUpViewModel::class.java)
            }

    private lateinit var binding: ActivitySignUpBinding
    private var fragmentContent: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.viewModel = signUpViewModel
        gotoInputPhoneNumberFragment()
        setupEvent()
    }

    private fun setupEvent() {
        signUpViewModel.onClickSignUpScreenListener = object : OnClickSignUpScreenListener {
            override fun clickBtnNextInputPhoneNumber() {
                gotoInputPasswordFragment()
            }

            override fun clickBtnNextInputPassword() {
                gotoInputInfoFragment()
            }

        }
    }

    private fun gotoInputPhoneNumberFragment() {
        fragmentContent = InputPhoneNumberFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContent, fragmentContent as InputPhoneNumberFragment).commit()
    }

    private fun gotoInputPasswordFragment() {
        fragmentContent = InputPasswordFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter,
            R.anim.exit,
            R.anim.pop_enter,
            R.anim.pop_exit
        )
        transaction.addToBackStack(null)
        transaction.add(R.id.fragmentContent, fragmentContent as InputPasswordFragment).commit()
    }

    private fun gotoInputInfoFragment() {
        fragmentContent = InputInfoFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter,
            R.anim.exit,
            R.anim.pop_enter,
            R.anim.pop_exit
        )
        transaction.addToBackStack(null)
        transaction.add(R.id.fragmentContent, fragmentContent as InputInfoFragment).commit()
    }
}