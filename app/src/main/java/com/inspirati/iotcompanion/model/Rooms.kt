package com.inspirati.iotcompanion.model


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose



class Rooms {

    @SerializedName("Bedroom")
    @Expose
    var bedroom: BedroomList? = null
    @SerializedName("Living Room")
    @Expose
    var livingRoom: LivingRoomList? = null
    @SerializedName("Kitchen")
    @Expose
    var kitchen: KitchenList? = null

}

