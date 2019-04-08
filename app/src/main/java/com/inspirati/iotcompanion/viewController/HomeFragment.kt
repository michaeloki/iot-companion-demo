package com.inspirati.iotcompanion.viewController


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.adapter.RoomArrayAdapter
import com.inspirati.iotcompanion.model.GetRooms
import com.inspirati.iotcompanion.model.RoomItem
import com.inspirati.iotcompanion.model.SharedPreferenceManager
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    var BEDLIST_STATUS_UPDATE = "populateBedList"
    var KITCHENLIST_STATUS_UPDATE = "populateKitchenList"
    var LIVINGLIST_STATUS_UPDATE = "populateLivingList"
    var GETROOM_STATUS_UPDATE = "getAllRooms"
    var BEDROOM_NAVIGATION = "BedroomNavigation"
    var KITCHEN_NAVIGATION = "KitchenNavigation"
    var LIVING_NAVIGATION = "LivingNavigation"


    lateinit var jsonArrayBed: JSONArray
    lateinit var jsonArrayKitchen: JSONArray
    lateinit var jsonArrayLiving: JSONArray

    private lateinit var apiInterface: APIInterface

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    var roomsList: ArrayList<RoomItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        //roomsList.clear()
        val prefMgr = SharedPreferenceManager(requireContext())

        prefMgr.setMyKey("title",getString(R.string.home_title))

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(itemMessageReceiver,
            IntentFilter(getString(R.string.navigate))
        )

        val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
        recyclerView = view!!.findViewById(R.id.list_rooms)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = itemArrayAdapter
        apiInterface = APIClient.getClient().create(APIInterface::class.java)

        getAllRooms()

        return view
    }


    private var itemMessageReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val itemPos = intent.getIntExtra("layoutPosition",0)

            if(itemPos == 0) {
                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(Intent(BEDROOM_NAVIGATION))
            }
            if(itemPos == 1) {
                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(Intent(KITCHEN_NAVIGATION))
            }
            if(itemPos == 2) {
                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(Intent(LIVING_NAVIGATION))
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

        if (roomsList.size == 0) {

        val preferenceManager = SharedPreferenceManager(context)
        call.enqueue(object : Callback<GetRooms> {
            override fun onResponse(call: Call<GetRooms>, response: Response<GetRooms>) {

                if (response.isSuccessful) {
                    try {
                        for (room in RoomsEnum.values()) {
                            var myRoom: String
                            if (room.toString() == getString(R.string.livingroomlabel)) {
                                myRoom = getString(R.string.livingroomtitle)
                            } else {
                                myRoom = room.toString()
                            }
                            roomsList.add(RoomItem(myRoom))
                            val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
                            recyclerView = view!!.findViewById(R.id.list_rooms)
                            recyclerView.layoutManager = LinearLayoutManager(context)
                            recyclerView.itemAnimator = DefaultItemAnimator()
                            recyclerView.adapter = itemArrayAdapter
                        }

                        val bed = response.body()?.rooms?.bedroom?.fixtures
                        val kitchen = response.body()?.rooms?.kitchen?.fixtures
                        val living = response.body()?.rooms?.livingRoom?.fixtures

                        jsonArrayBed = JSONArray(bed)
                        jsonArrayKitchen = JSONArray(kitchen)
                        jsonArrayLiving = JSONArray(living)

                        preferenceManager.setMyKey("bedroomData", jsonArrayBed.toString())
                        preferenceManager.setMyKey("kitchenData", jsonArrayKitchen.toString())
                        preferenceManager.setMyKey("livingRoomData", jsonArrayLiving.toString())

                        if (jsonArrayBed.length() > 0) {
                        }
                    } catch (exp: JSONException) {
                        Toast.makeText(requireContext(), exp.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<GetRooms>, t: Throwable) {
                progressBar.visibility = View.GONE

                val bedRoomData = preferenceManager.getMyKey(getString(R.string.bedroomData))
                if (bedRoomData.isNullOrBlank()) {
                    Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        for (room in RoomsEnum.values()) {
                            var myRoom: String
                            if (room.toString() == getString(R.string.livingroomlabel)) {
                                myRoom = getString(R.string.livingroomtitle)
                            } else {
                                myRoom = room.toString()
                            }
                            roomsList.add(RoomItem(myRoom))
                            val itemArrayAdapter = RoomArrayAdapter(R.layout.list_item_room, roomsList)
                            recyclerView = view!!.findViewById(R.id.list_rooms)
                            recyclerView.layoutManager = LinearLayoutManager(requireContext())
                            recyclerView.itemAnimator = DefaultItemAnimator()
                            recyclerView.adapter = itemArrayAdapter
                        }

                        val kitchen = preferenceManager.getMyKey(getString(R.string.kitchenData))
                        val living = preferenceManager.getMyKey(getString(R.string.livingRoomData))

                        jsonArrayBed = JSONArray(bedRoomData)
                        jsonArrayKitchen = JSONArray(kitchen)
                        jsonArrayLiving = JSONArray(living)

                        if (jsonArrayBed.length() > 0) {
                            BedFixturesFragment().bedList.clear()

                            try {
                                LocalBroadcastManager.getInstance(requireContext())
                                    .sendBroadcast(Intent(BEDLIST_STATUS_UPDATE))
                                LocalBroadcastManager.getInstance(requireContext())
                                    .sendBroadcast(Intent(KITCHENLIST_STATUS_UPDATE))
                                LocalBroadcastManager.getInstance(requireContext())
                                    .sendBroadcast(Intent(LIVINGLIST_STATUS_UPDATE))
                            } catch (e: Exception) {
                            }
                        }
                    } catch (exp: JSONException) {
                        Toast.makeText(requireContext(), exp.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
    }
}
