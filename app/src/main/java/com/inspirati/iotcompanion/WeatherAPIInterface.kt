package com.inspirati.iotcompanion


import com.inspirati.iotcompanion.model.*

import retrofit2.Call
import retrofit2.http.*


internal interface WeatherAPIInterface {

    @get:GET("/api/location/2165352/")
    val getWeather: Call<Weather>

}