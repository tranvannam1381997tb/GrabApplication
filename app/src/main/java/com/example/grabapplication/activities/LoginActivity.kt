package com.example.grabapplication.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.grabapplication.R
import com.example.grabapplication.common.afterTextChanged
import com.example.grabapplication.common.onEditorActionDone
import com.example.grabapplication.common.onEditorActionNext
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.customviews.ConfirmDialog
import com.example.grabapplication.databinding.ActivityLoginBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.LoginViewModel
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel
        by lazy {
            ViewModelProvider(this, BaseViewModelFactory(this)).get(LoginViewModel::class.java)
        }

    private lateinit var binding: ActivityLoginBinding

    private var jobStartLogin: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = loginViewModel
        setEventView()
    }

    private fun setEventView() {
        binding.username.apply {
            afterTextChanged {
                loginViewModel.validateDataLogin(
                    binding.username.text.toString(),
                    binding.password.text.toString()
                )
            }

            onEditorActionNext {
                binding.password.requestFocus()
            }
        }

        binding.password.apply {
            afterTextChanged {
                loginViewModel.validateDataLogin(
                    binding.username.text.toString(),
                    binding.password.text.toString()
                )
            }

            onEditorActionDone {
                if (loginViewModel.isEnableBtnLogin.get()!!) {
                    startLogin()
                }
            }
        }

        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            startLogin()
        }
    }

    private fun startLogin() {
        jobStartLogin =  GlobalScope.launch(Dispatchers.IO) {
            val completionHandler = HttpConnection.getInstance().startLogin(binding.username.text.toString(), binding.password.text.toString())
            if (!isActive) {
                return@launch
            }
            withContext(Dispatchers.Main) {
                 // TODO debug code
                completionHandler.error = null

                if (completionHandler.error != null) {
                    showDialogError(completionHandler.error)
                    binding.loading.visibility = View.GONE
                } else {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showDialogError(message: String?) {
        if (!message.isNullOrEmpty()) {
            val dialogError = ConfirmDialog(this)
            dialogError.setTextDisplay(getString(R.string.error), message, null, getString(R.string.label_ok))
            dialogError.setOnClickOK(View.OnClickListener {
                dialogError.dismiss()
            })
            dialogError.setHideLineButton()
            dialogError.show()
        }
    }
}