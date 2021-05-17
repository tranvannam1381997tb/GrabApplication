package com.example.grabapplication.googlemaps.models

data class Distance(
    var distanceText: String,
    var distanceValue: Int,
    var durationText: String,
    var durationValue: Int,
    var startAddress: String,
    var endAddress: String,
    var latStart: Double,
    var lngStart: Double,
    var latEnd: Double,
    var lngEnd: Double
)
