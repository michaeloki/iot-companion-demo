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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.inspirati.iotcompanion.MainActivity
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.adapter.BedFixtureArrayAdapter
import com.inspirati.iotcompanion.model.BedFixtureItem
import com.inspirati.iotcompanion.model.SharedPreferenceManager
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException


class BedFixturesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiInterface: APIInterface

    var bedList: ArrayList<BedFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bed_fixtures, container, false)

        bedList.clear()

        addTitle()

        val itemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
        recyclerView = view.findViewById(R.id.list_bed_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(MainActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = itemArrayAdapter


        val bedString = arguments!!.getString("myBedString")

        val jsonArrayBed = JSONArray(bedString)

        val prefMgr = SharedPreferenceManager(requireContext())
        prefMgr.setMyKey("bedString",bedString)
        val newJSON = JSONArray(prefMgr.getMyKey("bedroomFixtureStates"))
        val intent = Intent("switch-message")
        for (i in 0 until jsonArrayBed.length()) {
            val item = jsonArrayBed.getString(i)
                if (newJSON.getString(i) == "off") {
                    intent.putExtra("state", "false")
                    LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
                } else {
                    intent.putExtra("state", "true")
                    LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
                }
            Log.i("temp",prefMgr.getMyKey("the_temp"))

                bedList.add(BedFixtureItem(item,newJSON.getString(i),prefMgr.getMyKey("the_temp")))
                val newItemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
                recyclerView = view!!.findViewById(R.id.list_bed_fixtures)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.itemAnimator = DefaultItemAnimator()
                recyclerView.adapter = newItemArrayAdapter
        }

        try {

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                adjustTheAC,
                IntentFilter(MainActivity().adjustAirConditioner)
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchAction,
                IntentFilter("switch-message")
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                manualAction,
                IntentFilter("manual-switch-message")
            )
        } catch(npe:NullPointerException) {

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                adjustTheAC,
                IntentFilter(MainActivity().adjustAirConditioner)
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchAction,
                IntentFilter("switch-message")
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                manualAction,
                IntentFilter("manual-switch-message")
            )
        }
        return view
    }

    fun LoadBedroomList(bedString: String) {

        bedList.clear()

        val jsonArrayBed = JSONArray(bedString)


        val prefMgr = SharedPreferenceManager(requireContext())
        val newJSON = JSONArray(prefMgr.getMyKey("bedroomFixtureStates"))
        val intent = Intent("switch-message")
        for (i in 0 until jsonArrayBed.length()) {
            val item = jsonArrayBed.getString(i)

            if(newJSON.getString(i) == "off") {
                intent.putExtra("state","false")
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            } else {
                intent.putExtra("state","true")
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            }

            bedList.add(BedFixtureItem(item,newJSON.getString(i),prefMgr.getMyKey("the_temp")))
            val newItemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
            recyclerView = view!!.findViewById(R.id.list_bed_fixtures)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = newItemArrayAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        addTitle()
    }

    fun addTitle() {
        val prefMgr = SharedPreferenceManager(requireContext())
        prefMgr.setMyKey("title",getString(R.string.bedroom_fixtures_title))
    }

    private val switchAction: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            try {
                val bedSwitch: Switch = view!!.findViewById(R.id.bedSwitch)
                if (intent?.getStringExtra("state") == "false") {
                    bedSwitch.isChecked = false
                }

                if (intent?.getStringExtra("state") == "true") {
                    bedSwitch.isChecked = true
                }
            } catch (npe: NullPointerException){}
        }
    }

    private val manualAction: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val bedSwitch: Switch = view!!.findViewById(R.id.bedSwitch)
                val pos = intent!!.getStringExtra("position")
                val state = intent!!.getStringExtra("state")

                if (state == "on") {
                    bedSwitch.isChecked = true
                }
                if (state == "off") {
                    bedSwitch.isChecked = false
                }
            val prefMgr = SharedPreferenceManager(requireContext())
            val newJSONList = JSONArray(prefMgr.getMyKey("bedroomFixtureStates"))
            newJSONList.put(pos.toInt(),state)
            prefMgr.setMyKey("bedroomFixtureStates", newJSONList.toString())
            setSwitch(state,pos.toInt())
            } catch (npe:NullPointerException) {}
        }
    }

    private val adjustTheAC = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val temp = intent?.getStringExtra("the_temp")
            if(temp?.toDouble()!! >= 25.01) {
                try {
                    val prefMgr = SharedPreferenceManager(requireContext())
                    prefMgr.setMyKey("the_temp", temp)
                    val newJSON = JSONArray(prefMgr.getMyKey("bedroomData"))
                    newJSON.remove(2)
                    newJSON.put(2, "AC $temp")
                    prefMgr.setMyKey("bedroomData", newJSON.toString())
                    LoadBedroomList(prefMgr.getMyKey("bedString"))
                } catch (exp:Exception) {}
            }
        }
    }

    fun setSwitch(state: String,position: Int) {
        apiInterface = APIClient.getClient().create(APIInterface::class.java)
        when (position){
            0 -> {
                var call = apiInterface.turnOnBedLightOne()
                if(state=="off") {
                    call = apiInterface.turnOffBedLightOne()
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
                var call = apiInterface.turnOnBedLightTwo()
                if(state=="off") {
                    call = apiInterface.turnOffBedLightTwo()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Toast.makeText(requireContext(), getString(R.string.successMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
            2 -> {
                var call = apiInterface.turnOnAC()
                if(state=="off") {
                    call = apiInterface.turnOffAC()
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
