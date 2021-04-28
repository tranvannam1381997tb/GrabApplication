package com.example.grabapplication.customviews

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.TextView
import com.example.grabapplication.R
import com.example.grabapplication.common.setOnSingleClickListener

/**
 * Created by NamTV on 27/3/20.
 */
class ConfirmDialog(context: Context) : Dialog(context) {
    private val btnOK: TextView
    private val btnCancel: TextView
    private val txtDlgApplyMsg: TextView
    private val txtDlgApplyTitle: TextView
    private val lineButton: TextView
    private val layoutMessage: ScrollView

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_confirm)
        btnOK = findViewById(R.id.btnOk)
        btnCancel = findViewById(R.id.btnCancel)
        lineButton = findViewById(R.id.lineButton)
        txtDlgApplyMsg = findViewById(R.id.txtMessage)
        txtDlgApplyTitle = findViewById(R.id.txtTitle)
        layoutMessage = findViewById(R.id.layout_message)
        btnCancel.setOnSingleClickListener(View.OnClickListener { dismiss() })
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
    }

    fun setTextDisplay(title: String?, message: String?, btnCancel: String?, btnOK: String?) {
        if (message.isNullOrEmpty()) {
            layoutMessage.visibility = View.GONE
        } else {
            layoutMessage.visibility = View.VISIBLE
            txtDlgApplyMsg.text = message
        }
        if (title.isNullOrEmpty()) {
            txtDlgApplyTitle.visibility = View.GONE
        } else {
            txtDlgApplyTitle.visibility = View.VISIBLE
            txtDlgApplyTitle.text = title
        }
        if (btnOK != null && btnOK.isNotEmpty()) {
            this.btnOK.text = btnOK
        }
        if (btnCancel != null && btnCancel.isNotEmpty()) {
            this.btnCancel.text = btnCancel
        } else {
            this.btnCancel.visibility = View.GONE
        }
    }

    fun setColorTitle(color: Int) {
        txtDlgApplyTitle.setTextColor(color)
    }

    fun setColorBtnOK(color: Int) {
        btnOK.setTextColor(color)
    }

    fun setHideLineButton() {
        lineButton.visibility = View.GONE
    }

    fun setOnClickOK(listener: View.OnClickListener) {
        btnOK.setOnClickListener(listener)
    }

    fun setOnClickCancel(listener: View.OnClickListener) {
        btnCancel.setOnClickListener(listener)
    }

    fun setTextTypeBoldBtnOK() {
        btnCancel.typeface = Typeface.DEFAULT
        btnOK.typeface = Typeface.DEFAULT_BOLD
    }

    fun setTxtMessageBold() {
        txtDlgApplyMsg.typeface = Typeface.DEFAULT_BOLD
    }
}
