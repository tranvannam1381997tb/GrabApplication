package com.example.grabapplication.common

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.grabapplication.model.TypeDriverValue
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class CommonUtils {
    companion object {

        fun getJsonArrayFromJsonObject(jsonObject: JSONObject, key: String): JSONArray {
            return if (jsonObject.has(key)) {
                jsonObject.getJSONArray(key)
            } else {
                JSONArray()
            }
        }

        fun getJsonObjectFromJsonObject(jsonObject: JSONObject, key: String): JSONObject {
            return if (jsonObject.has(key)) {
                jsonObject.getJSONObject(key)
            } else {
                JSONObject()
            }
        }

        fun getStringFromJsonObject(jsonObject: JSONObject, key: String): String {
            return if (jsonObject.has(key)) {
                jsonObject.getString(key)
            } else {
                ""
            }
        }

        fun getDoubleFromJsonObject(jsonObject: JSONObject, key: String): Double {
            var data: Double? = null
            if (jsonObject.has(key)) {
                data = jsonObject.getString(key).toDoubleOrNull()
            }
            if (data == null) {

                data = 0.0
            }
            return data
        }

        fun getIntFromJsonObject(jsonObject: JSONObject, key: String) : Int {
            return if (jsonObject.has(key)) {
                jsonObject.getInt(key)
            } else {
                0
            }
        }

        fun getBooleanFromJsonObject(jsonObject: JSONObject, key: String) : Boolean {
            return if (jsonObject.has(key)) {
                jsonObject.getBoolean(key)
            } else {
                false
            }
        }

        fun getDateFromJsonObject(jsonObject: JSONObject, key: String): String {
            if (jsonObject.has(key)) {
                return convertStringToDate(jsonObject.getString(key))
            }
            return ""
        }

        @SuppressLint("SimpleDateFormat")
        private fun convertStringToDate(dateString: String): String {
            try {
                val formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_FOR_FIREBASE)
                val date = formatter.parse(dateString)

                return DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_APP).format(date)
            } catch (e: Exception) {
                Log.d("NamTV", "CommonUtils::convertStringToDate: exception = $e")
            }

            return ""
        }

        fun getTypeDriver(jsonObject: JSONObject, key: String): String {
            val typeDriver =  if (jsonObject.has(key)) {
                jsonObject.getInt(key)
            } else 0
            if (typeDriver == 0) {
                return TypeDriverValue.GRAB_BIKE.rawValue
            }
            if (typeDriver == 1) {
                return TypeDriverValue.GRAB_CAR.rawValue
            }
            return ""
        }

        fun clearFocusEditText(activity: Activity) {
            val view = activity.currentFocus
            if (view != null && view is EditText) {
                hideKeyboard(activity)
                // Resign focus EditText
                view.isFocusable = false
                view.isFocusable = true
                view.isFocusableInTouchMode = true
            }
        }

        private fun hideKeyboard(activity: Activity){
            val inputMethod: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethod.hideSoftInputFromWindow(activity.currentFocus!!.windowToken!!, 0)
        }

        fun getTimeArrived(calendar: Calendar): String {
            val dateFormat = SimpleDateFormat("HH:mm")
            return dateFormat.format(calendar.time)
        }
    }
}