package com.inspirati.iotcompanion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.inspirati.iotcompanion.adapter.RoomArrayAdapter
import com.inspirati.iotcompanion.viewController.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.R.attr.keySet
//import jdk.nashorn.internal.objects.NativeArray.forEach
import android.R.attr.keySet
import android.R.attr.keySet
import com.inspirati.iotcompanion.adapter.BedFixtureArrayAdapter
import com.inspirati.iotcompanion.model.*
import com.inspirati.iotcompanion.viewController.BedFixturesFragment


class MainActivity : AppCompatActivity() {

    private lateinit var apiInterface: APIInterface

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    var BEDLIST_STATUS_UPDATE = "populateBedList"
    var KITCHENLIST_STATUS_UPDATE = "populateKitchenList"
    var LIVINGLIST_STATUS_UPDATE = "populateLivingList"
    lateinit var jsonArrayBed: JSONArray

    var roomsList: ArrayList<RoomItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LocalBroadcastManager.getInstance(this).registerReceiver(itemMessageReceiver,
            IntentFilter("room-intent")
        )

        progressBar = findViewById(R.id.roomsProgress)

        val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
        recyclerView = findViewById(R.id.list_rooms)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = itemArrayAdapter
        apiInterface = APIClient.getClient().create(APIInterface::class.java)

        LocalBroadcastManager.getInstance(this).registerReceiver(itemMessageReceiver,
            IntentFilter("navigation-message")
        )

        getAllRooms()
    }

    private var itemMessageReceiver: BroadcastReceiver = object:BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val itemPos = intent.getIntExtra("layoutPosition",0)
            val roomInfo = roomsList[itemPos].messageText

            if(itemPos == 0) {
                val roomIntent = Intent(this@MainActivity, BedFixturesFragment::class.java)
                roomIntent.putExtra("itemPosition", roomInfo)
                startActivity(roomIntent)
            }
            if(itemPos == 1) {
                val roomIntent = Intent(this@MainActivity, KitchenFixturesFragment::class.java)
                roomIntent.putExtra("itemPosition", roomInfo)
                startActivity(roomIntent)
            }
            if(itemPos == 2) {
                val roomIntent = Intent(this@MainActivity, LivingFixturesFragment::class.java)
                roomIntent.putExtra("itemPosition", roomInfo)
                startActivity(roomIntent)
            }


            var position =0
            when (position) {
                1 -> position = 0
                2 -> println("Tuesday")
            }


        }
    }

    internal enum class RoomsEnum {
        Bedroom,
        Kitchen,
        LIVINGROOM
    }

    fun getAllRooms () {
        val call = apiInterface.allRooms
        call.enqueue(object : Callback<GetRooms> {
            override fun onResponse(call: Call<GetRooms>, response: Response<GetRooms>) {

                if (response.isSuccessful) {
                    try {
                        for (room in RoomsEnum.values()) {
                            var myRoom: String
                            if(room.toString() == "LIVINGROOM"){
                                myRoom = "Living Room"
                            }  else {
                                myRoom = room.toString()
                            }
                            roomsList.add(RoomItem(myRoom))
                            val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
                            recyclerView = findViewById(R.id.list_rooms)
                            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                            recyclerView.itemAnimator = DefaultItemAnimator()
                            recyclerView.adapter = itemArrayAdapter

                            val bed = response.body()?.rooms?.bedroom?.fixtures
                            val kitchen = response.body()?.rooms?.kitchen?.fixtures
                            val living = response.body()?.rooms?.livingRoom?.fixtures

                            /*val firstLight = bed[0]

                            bed?.forEach {
                                it
                                BedFixturesFragment().bedList.add(BedFixtureItem(item,,))
                                val itemArrayAdapter = CarArrayAdapter(R.layout.list_item_car, carsList)
                                recyclerView = findViewById(R.id.list_cars)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                recyclerView.itemAnimator = DefaultItemAnimator()
                                recyclerView.adapter = itemArrayAdapter
                            }*/

                            jsonArrayBed = JSONArray(bed)

                            if (jsonArrayBed.length() > 0) {
                                BedFixturesFragment().bedList.clear()
                                //val myBedList = BedFixturesFragment().bedList


                                Log.i("getALL12222LIV",jsonArrayBed.get(0).toString())


                                    //val objectKey = jsonArrayBed.getJSONObject(i)

                                    //val status = objectKey.getString("status")

                                    try {
                                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(BEDLIST_STATUS_UPDATE))
                                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(KITCHENLIST_STATUS_UPDATE))
                                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(LIVINGLIST_STATUS_UPDATE))
                                    } catch(e:Exception) {
                                    }

                            }
                            Log.i("getALL11122LIV",bed.toString())
                            /*
                            roomsList.add(RoomItem(Rooms.BEDROOM.toString()))
                            val itemFixtureArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
                            recyclerView = findViewById(R.id.list_rooms)
                            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                            recyclerView.itemAnimator = DefaultItemAnimator()
                            recyclerView.adapter = itemFixtureArrayAdapter*/
                        }

                    } catch (exp: JSONException) {
                        Toast.makeText(this@MainActivity, exp.localizedMessage , Toast.LENGTH_LONG).show()
                    }


/*
                    val bedroomList: BedroomList? = response.body()?.rooms?.bedroom

                    val kitchenList: KitchenList? = response.body()?.rooms?.kitchen

                    val livingRoomList: LivingRoomList? = response.body()?.rooms?.livingRoom

                    //Log.i("getALL11555",bedroomList?.toString())
                    Log.i("getALL11122BED",bedroomList?.fixtures?.size.toString())
                    Log.i("getALL11122KIT",kitchenList?.fixtures?.size.toString())
                    Log.i("getALL11122LIV",livingRoomList?.fixtures?.size.toString())*/
                }
            }

            override fun onFailure(call: Call<GetRooms>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, t.toString() , Toast.LENGTH_SHORT).show()
                Log.i("getALL111",t.message)

            }
        })
    }

    /*
    fun getAvailableRooms () {
        progressBar.visibility = View.VISIBLE
        //val rooms = Rooms()
        val call = apiInterface.allRooms

        call.enqueue(object : Callback<Rooms> {
            override fun onFailure(call: Call<Rooms>?, t: Throwable?) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, t.toString() , Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Rooms>?, response: Response<Rooms>?) {

                progressBar.visibility = View.GONE


                var roomPayload = response!!.body()?.getRooms().toString()
                var getAllMyRooms = roomPayload.replace("=",":")
                val allRooms = getAllMyRooms.replace("Living ", "Living")


                try {
                    var jsonPayload =  JSONObject(allRooms)

                    Log.i("getAllRoomsOBJ",jsonPayload.toString())
                    val keys = jsonPayload.names()

                    for (i in 0 until keys.length()) {

                        val key = keys.getString(i) // Here's your key
                        Log.i("getAllRooms1 key is",key.toString())
                        val value = jsonPayload.getString(key) // Here's your value
                        Log.i("getAllRooms1 value is",value.toString())
                        roomsList.add(RoomItem(key.toString()))
                        val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
                        recyclerView = findViewById(R.id.list_rooms)
                        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                        recyclerView.itemAnimator = DefaultItemAnimator()
                        recyclerView.adapter = itemArrayAdapter
                    }

                } catch (exp: JSONException) {
                    Toast.makeText(this@MainActivity, exp.localizedMessage , Toast.LENGTH_LONG).show()
                }
               // val reader = JSONObject(roomPayload)
               // Log.i("getAllRooms2",roomPayload?.toString())

            }
        })
    }*/

    /*var roomsIndex = roomPayload!!.allMyRooms

    val roomsArrayIndex = JSONArray(roomsIndex)

    if (roomsArrayIndex.length() > 0) {
        roomsList.clear()
        for (i in 0 until roomsArrayIndex.length()) {
            var carIndex = roomsArrayIndex.getString(i)
            roomsList.add(RoomItem(carIndex))
        }
    }*/


    //var roomsObj = JSONObject(roomPayload?.getRooms())

    //var roomsIndex = JSONObject(roomPayload?.getRooms())

    //val roomsArrayIndex = JSONArray(roomPayload?.getRooms())

    //Log.i("getAllRooms333",reader.toString())


    //val jsonArr: JSONObject = JSONObject(roomsObj)
/*
                Log.i("getAllRooms333",jsonArr.length().toString())

                if (jsonArr.length() > 0) {
                    roomsList.clear()

                    for (i in 0 until jsonArr.length()) {
                        var room = jsonArr.getString(i)

                        roomsList.add(RoomItem(room))
                        val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
                        recyclerView = findViewById(R.id.list_rooms)
                        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                        recyclerView.itemAnimator = DefaultItemAnimator()
                        recyclerView.adapter = itemArrayAdapter
                    }

                }*/
}
