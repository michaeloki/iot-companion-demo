package com.inspirati.iotcompanion.model


import android.util.Log
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import com.google.gson.annotations.Expose


//////////////////////
//
/*
class Rooms {
    @SerializedName("rooms")
    var allMyRooms:Any? = null

    fun getRooms(): Any? { //Any?
        return allMyRooms
    }
}*/
///////////////////
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

/*
class Rooms {
    //@SerializedName("rooms")
    @SerializedName("rooms")
    //@Expose
    private var rooms: Rooms? = null

    fun getRooms(): Rooms? {
        return rooms
    }

    fun setRooms(rooms: Rooms) {
        this.rooms = rooms
    }
    //lateinit var allMyRooms:String
    //var data: List<RoomList> = ArrayList()
    //var data: List<Datum> = ArrayList()

    inner class RoomDataList {

        @SerializedName("Bedroom")
        var id: Int? = null
        @SerializedName("first_name")
        var first_name: String? = null
        @SerializedName("last_name")
        var last_name: String? = null
        @SerializedName("avatar")
        var avatar: String? = null

    }*/
    /* var wkda: ArrayList<Datum>? = null

     inner class Datum {
         @SerializedName("data")
         var data: Any? = null
     }*/

    /*fun getData(): String {
        return allMyRooms
    }*/

    //var wkda: ArrayList<Datum>? = null
/*
    @SerializedName("data")
    var data: List<Datum> = ArrayList()
*/


    /* @SerializedName("wkda")
     var data: List<Datum> = ArrayList()

     inner class Datum {

         @SerializedName("id")
         var id: Int? = null
         @SerializedName("first_name")
         var first_name: String? = null
         @SerializedName("last_name")
         var last_name: String? = null
         @SerializedName("avatar")
         var avatar: String? = null

     }*/


    //var getResult = JSONArray(wkda)

    /*var wkdaList: List<Datum> = ArrayList()

    inner class Datum {
        @SerializedName("")
        var wkda: Any? = null
    }*/

//}

//class CarManufacturers {
/*
    @SerializedName("page")
    var page: Int? = null
    @SerializedName("per_page")
    var perPage: Int? = null
    @SerializedName("total")
    var total: Int? = null
    @SerializedName("total_pages")
    var totalPages: Int? = null*/
// @SerializedName("wkda")
// var wkda: List<Datum> = ArrayList()

//  inner class Datum {

//    @SerializedName("")
//    var id: Int? = null
/*
@SerializedName("pageSize")
var name: String? = null
@SerializedName("year")
var year: Int? = null
@SerializedName("pantone_value")
var pantoneValue: String? = null*/

//  }
//}

