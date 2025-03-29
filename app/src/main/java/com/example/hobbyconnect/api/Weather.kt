package com.example.hobbyconnect.api

import com.google.gson.Gson

data class Weather(
    val lat: String,
    val lon: String,
    val elevation: Int,
    val timezone: String,
    val units: String,
    val current: Current,
) {
    companion object {
        fun fromJson(request: String): Weather = Gson().fromJson(request, Weather::class.java)
    }
}

data class Current(
    val icon: String,
    val icon_num: Int,
    val summary: String,
    val temperature: Double,
    val wind: Wind,
    val precipitation: Precipitation,
    val cloud_cover: Int,
)

data class Wind(
    val speed: Double,
    val angle: Int,
    val dir: String,
)

data class Precipitation(
    val total: Int,
    val type: String,
)
