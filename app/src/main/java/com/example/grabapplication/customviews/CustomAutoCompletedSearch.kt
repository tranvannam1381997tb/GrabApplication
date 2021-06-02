package com.example.grabapplication.customviews

import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.grabapplication.R
import com.google.android.libraries.places.widget.AutocompleteSupportFragment

class CustomAutoCompletedSearch : AutocompleteSupportFragment() {
    lateinit var btnClear: View
    lateinit var editText: EditText

    override fun onViewCreated(p0: View, p1: Bundle?) {
        super.onViewCreated(p0, p1)
        btnClear = p0.findViewById(R.id.places_autocomplete_clear_button)
        editText = p0.findViewById(R.id.places_autocomplete_search_input)
    }
}
