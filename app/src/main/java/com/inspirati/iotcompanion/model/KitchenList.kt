package com.inspirati.iotcompanion.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class KitchenList {

    @SerializedName("fixtures")
    @Expose
    var fixtures: List<String>? = null

}