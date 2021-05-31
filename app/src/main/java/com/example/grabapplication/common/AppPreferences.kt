package com.example.grabapplication.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.grabapplication.model.BookInfo
import com.example.grabapplication.model.DriverInfo
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by NamTV on 27/3/20.
 */
@SuppressLint("CommitPrefEdits")
class AppPreferences private constructor(context: Context) {
    private var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var editor: SharedPreferences.Editor = prefs.edit()

    var bookInfoPreferences: BookInfo? = null
        get() {
            val gson = Gson()
            val json = prefs.getString(BOOK_INFO, "")
            return gson.fromJson(json, BookInfo::class.java)
        }
        set(value) {
            field = value
            val gson = Gson()
            val json = gson.toJson(value)
            editor.putString(BOOK_INFO, json).commit()
        }

    var jsonDataBookInfo: JSONObject = JSONObject()
        get() {
            val json = prefs.getString(JSON_DATA_BOOK_INFO, "{}")
            return JSONObject(json!!)
        }
        set(value) {
            field = value
            editor.putString(JSON_DATA_BOOK_INFO, value.toString()).commit()
        }

    var priceOfKilometer: Int = 5000
        get() {
            return prefs.getInt(PRICE_OF_KILOMETER, 5000)
        }
        set(value) {
            field = value
            editor.putInt(PRICE_OF_KILOMETER, value).commit()
        }

    var ratePoint: Float = 0.5F
        get() {
            return prefs.getFloat(RATE_POINT, 0.5F)
        }
        set(value) {
            field = value
            editor.putFloat(RATE_POINT, value).commit()
        }

    var distancePoint: Float = 0.5F
        get() {
            return prefs.getFloat(DISTANCE_POINT, 0.5F)
        }
        set(value) {
            field = value
            editor.putFloat(DISTANCE_POINT, value).commit()
        }

    var agePoint: Float = 0.5F
        get() {
            return prefs.getFloat(AGE_POINT, 0.5F)
        }
        set(value) {
            field = value
            editor.putFloat(AGE_POINT, value).commit()
        }

    companion object : SingletonHolder<AppPreferences, Context>(::AppPreferences) {
        const val BOOK_INFO = "bookInfo"
        const val JSON_DATA_BOOK_INFO = "jsonDataBookInfo"
        const val PRICE_OF_KILOMETER = "priceOfKilometer"
        const val RATE_POINT = "ratePoint"
        const val DISTANCE_POINT = "distancePoint"
        const val AGE_POINT = "agePoint"
    }
}