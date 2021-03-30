package com.example.grabapplication.common

import org.json.JSONArray
import org.json.JSONObject

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

        fun getFloatFromJsonObject(jsonObject: JSONObject, key: String): Float {
            var data: Float? = null
            if (jsonObject.has(key)) {
                data = jsonObject.getString(key).toFloatOrNull()
            }
            if (data == null) {

                data = 0F
            }
            return data
        }
    }
}