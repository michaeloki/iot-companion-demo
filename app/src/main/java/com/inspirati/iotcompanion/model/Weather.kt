package com.inspirati.iotcompanion.model


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Weather {

    @SerializedName("consolidated_weather")
    @Expose
    var consolidatedWeather: List<ConsolidatedWeather>? = null
    @SerializedName("time")
    @Expose
    var time: String? = null
    @SerializedName("sun_rise")
    @Expose
    var sunRise: String? = null
    @SerializedName("sun_set")
    @Expose
    var sunSet: String? = null
    @SerializedName("timezone_name")
    @Expose
    var timezoneName: String? = null
    @SerializedName("parent")
    @Expose
    var parent: Parent? = null
    @SerializedName("sources")
    @Expose
    var sources: List<Source>? = null
    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("location_type")
    @Expose
    var locationType: String? = null
    @SerializedName("woeid")
    @Expose
    var woeid: Int? = null
    @SerializedName("latt_long")
    @Expose
    var lattLong: String? = null
    @SerializedName("timezone")
    @Expose
    var timezone: String? = null

}