package com.inspirati.iotcompanion.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetRooms {

    @SerializedName("rooms")
    @Expose
    var rooms: Rooms? = null
}