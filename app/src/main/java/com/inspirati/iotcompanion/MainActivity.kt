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
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.inspirati.iotcompanion.adapter.RoomArrayAdapter
import com.inspirati.iotcompanion.viewController.*
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

                            jsonArrayBed = JSONArray(bed)

                            if (jsonArrayBed.length() > 0) {
                                BedFixturesFragment().bedList.clear()

                                    try {
                                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(BEDLIST_STATUS_UPDATE))
                                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(KITCHENLIST_STATUS_UPDATE))
                                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(LIVINGLIST_STATUS_UPDATE))
                                    } catch(e:Exception) {
                                    }
                            }

                        }

                    } catch (exp: JSONException) {
                        Toast.makeText(this@MainActivity, exp.localizedMessage , Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<GetRooms>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, t.toString() , Toast.LENGTH_SHORT).show()
            }
        })
    }
}
