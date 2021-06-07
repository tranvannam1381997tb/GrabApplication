package com.example.grabapplication.common

import com.example.grabapplication.model.DriverInfo
import com.google.android.gms.maps.model.LatLng

class Constants {
    companion object {
        const val DEFAULT_ZOOM_MAPS = 15

        const val DATE_FORMAT_SERVER = "yyyy-MM-dd'T'HH:mm:ss.SSSXXXXX"
        const val DATE_FORMAT_APP = "dd, MMM yyyy"
        const val KEY_SUCCESS = "success"

        val DEFAULT_LOCATION = LatLng(-33.8523341, 151.2106085)

        val driverInfoDefault = DriverInfo(
                driverId = "60b650cdfcf59c2d60305f27",
                tokenId = "ef3PWKMNTIeVNUIIvP6VTh:APA91bG8uhZ-drNTJiO--o3Wuou7bvnYYSgxmrqpEydT2_9E2o29hhh1b_C_Zl3rNbwRnYuQCETg-mhV7Zf8MXYNHExCGydcarcBBQkzIwSh6hly7D4nb4t5KkEylYF1W2xXKYRhCUsf",
                name = "Trần Văn Nam test",
                age = 25,
                sex = "Nam",
                phoneNumber = "023123123",
                latitude = 20.9900287,
                longitude = 105.7978594,
                rate = 4.5F,
                status = 0,
                startDate = "2021-06-01T22:20:15.975+07:00",
                typeDriver = "0",
                typeVehicle = "Honda Wawe",
                licensePlateNumber = "123-123",
                distance = 1.783F,
                point = 3F
        )

        const val FRAGMENT_MAP = 0
        const val FRAGMENT_INFO_DRIVER = 1
        const val FRAGMENT_FIND_PLACE = 2
        const val FRAGMENT_WAIT_DRIVER = 3
        const val FRAGMENT_DRIVER_GOING = 4
        const val FRAGMENT_BILL = 5
        const val FRAGMENT_DRIVER_SUGGEST = 6

        const val TIME_WAIT_DRIVER: Long = 60 * 1000
        const val COUNT_DOWN_INTERVAL: Long = 1 * 1000

        const val REQUEST_GET_LIST_DRIVER = 100
        const val REQUEST_GET_POLICY = 101
        const val TIME_SCHEDULE_GET_LIST_DRIVER = 60*1000
    }
}