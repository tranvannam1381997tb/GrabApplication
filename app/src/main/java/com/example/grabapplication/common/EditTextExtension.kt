package com.example.grabapplication.common

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.example.grabapplication.googlemaps.MapsConnection

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

fun EditText.onFocusChanged(afterFocusChanged: (String) -> Unit) {
    this.onFocusChangeListener = View.OnFocusChangeListener { _, _ ->
        afterFocusChanged.invoke(this.text.toString())
    }
}

fun EditText.onEditorActionDone(onEditorActionDone: (String) -> Unit) {
    this.setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> {
                onEditorActionDone.invoke(this.text.toString())
            }
        }
        false
    }
}

fun EditText.onEditorActionNext(onEditorActionDone: (String) -> Unit) {
    this.setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_NEXT -> {
                onEditorActionDone.invoke(this.text.toString())
            }
        }
        false
    }
}