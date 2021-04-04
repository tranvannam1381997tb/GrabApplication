package com.example.grabapplication.activities

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.grabapplication.R
import com.example.grabapplication.connecttion.HttpConnection
import com.example.grabapplication.customviews.DialogApplyConfirm
import com.example.grabapplication.databinding.ActivityLoginBinding
import com.example.grabapplication.viewmodel.BaseViewModelFactory
import com.example.grabapplication.viewmodel.LoginViewModel
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel
        by lazy {
            ViewModelProvider(this, BaseViewModelFactory(this, this)).get(LoginViewModel::class.java)
        }

    private lateinit var binding: ActivityLoginBinding

    private var jobStartLogin: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = loginViewModel

        binding.username.afterTextChanged {
            loginViewModel.validateDataLogin(binding.username.text.toString(), binding.password.text.toString())
        }

        binding.password.apply {
            afterTextChanged {
                loginViewModel.validateDataLogin(binding.username.text.toString(), binding.password.text.toString())
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if (loginViewModel.isEnableBtnLogin.get()!!) {
                            startLogin()
                        }
                }
                false
            }

            binding.login.setOnClickListener {
                binding.loading.visibility = View.VISIBLE
                startLogin()
            }
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
            val dialogError = DialogApplyConfirm(this)
            dialogError.setTextDisplay(getString(R.string.error), message, null, getString(R.string.label_ok))
            dialogError.setOnClickOK(View.OnClickListener {
                dialogError.dismiss()
            })
            dialogError.setHideLineButton()
            dialogError.show()
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}