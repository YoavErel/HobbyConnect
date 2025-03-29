package com.example.hobbyconnect.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class WeatherService {

    companion object {

        private const val REQUEST_URL =
            "https://www.meteosource.com/api/v1/free/point?lat=32.8184&lon=34.999168&sections=current%2Chourly&language=en&units=auto&key=j9w2gj2orsgnu3zx985fjj3rnuz6anm61ksr94tv"
    }

    suspend fun fetchWeather(): Weather = withContext(Dispatchers.IO) {
        try {
            val responseJson = URL(REQUEST_URL).readText()
            Log.d("WeatherService", "Raw JSON: $responseJson")
            Weather.fromJson(responseJson) // Parse JSON into the Weather model
        } catch (e: Exception) {
            Log.e("WeatherService", "Error fetching weather: ${e.localizedMessage}", e)
            throw e
        }
    }
}
