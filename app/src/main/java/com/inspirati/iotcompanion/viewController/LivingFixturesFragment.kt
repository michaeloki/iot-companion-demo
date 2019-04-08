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
import android.widget.Switch
import android.widget.Toast
import com.inspirati.iotcompanion.MainActivity
import com.inspirati.iotcompanion.R

import com.inspirati.iotcompanion.adapter.LivingFixtureArrayAdapter

import com.inspirati.iotcompanion.model.LivingFixtureItem
import com.inspirati.iotcompanion.model.SharedPreferenceManager
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException


class LivingFixturesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiInterface: APIInterface

    var livingRoomList: ArrayList<LivingFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_living_fixtures, container, false)

        livingRoomList.clear()

        addTitle()

        val itemArrayAdapter = LivingFixtureArrayAdapter(R.layout.list_item_living_fixture, livingRoomList)
        recyclerView = view.findViewById(R.id.list_living_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(MainActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = itemArrayAdapter

        val livingString = arguments!!.getString("myLivingRoomString")

        val jsonArrayLiving = JSONArray(livingString)

        val prefMgr = SharedPreferenceManager(requireContext())
        prefMgr.setMyKey("livingString",livingString)
        val newJSON = JSONArray(prefMgr.getMyKey("livingFixtureStates"))
        val intent = Intent("living-switch-message")
        for (i in 0 until jsonArrayLiving.length()) {
            val item = jsonArrayLiving.getString(i)
            if(newJSON.getString(i) == "off") {
                intent.putExtra("state","false")
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            } else {
                intent.putExtra("state","true")
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            }

            livingRoomList.add(LivingFixtureItem(item,newJSON.getString(i)))
            val livingItemArrayAdapter = LivingFixtureArrayAdapter(R.layout.list_item_living_fixture, livingRoomList)
            recyclerView = view!!.findViewById(R.id.list_living_fixtures)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = livingItemArrayAdapter
        }

        try {

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchAction,
                IntentFilter("living-switch-message")
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                manualAction,
                IntentFilter("manual-living-switch-message")
            )
        } catch(npe: NullPointerException) {

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchAction,
                IntentFilter("living-switch-message")
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                manualAction,
                IntentFilter("manual-living-switch-message")
            )
        }

        return view
    }

    fun addTitle() {
        val prefMgr = SharedPreferenceManager(requireContext())
        prefMgr.setMyKey("title",getString(R.string.living_room_fixtures_title))
    }


    private val switchAction: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val livingSwitch: Switch = view!!.findViewById(R.id.livingSwitch)
                if (intent?.getStringExtra("state") == "false") {
                    livingSwitch.isChecked = false
                }

                if (intent?.getStringExtra("state") == "true") {
                    livingSwitch.isChecked = true
                }
            } catch (npe: NullPointerException){}
        }
    }

    private val manualAction: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val livingSwitch: Switch = view!!.findViewById(R.id.livingSwitch)
                val pos = intent!!.getStringExtra("position")
                val state = intent!!.getStringExtra("state")

                if (state == "on") {
                    livingSwitch.isChecked = true
                }
                if (state == "off") {
                    livingSwitch.isChecked = false
                }

                val prefMgr = SharedPreferenceManager(requireContext())
                val newJSONList = JSONArray(prefMgr.getMyKey("livingFixtureStates"))
                newJSONList.put(pos.toInt(),state)
                prefMgr.setMyKey("livingFixtureStates", newJSONList.toString())
                setLivingSwitch(state,pos.toInt())
            } catch(exp:Exception){}

        }
    }

    fun setLivingSwitch(state: String,position: Int) {
        apiInterface = APIClient.getClient().create(APIInterface::class.java)
        when (position){
            0 -> {
                var call = apiInterface.turnOnLivingLightOne()
                if(state=="off") {
                    call = apiInterface.turnOffLivingLightOne()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Toast.makeText(requireContext(), getString(R.string.successMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
            1 -> {
                var call = apiInterface.turnOnLivingLightTwo()
                if(state=="off") {
                    call = apiInterface.turnOnLivingLightTwo()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Toast.makeText(requireContext(), getString(R.string.successMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
