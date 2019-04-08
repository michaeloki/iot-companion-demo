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
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.inspirati.iotcompanion.MainActivity
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.adapter.KitchenFixtureArrayAdapter
import com.inspirati.iotcompanion.model.KitchenFixtureItem
import org.json.JSONArray
import com.inspirati.iotcompanion.model.SharedPreferenceManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException


class KitchenFixturesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiInterface: APIInterface

    var kitchenList: ArrayList<KitchenFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kitchen_fixtures, container, false)

        kitchenList.clear()

        addTitle()

        val itemArrayAdapter = KitchenFixtureArrayAdapter(R.layout.list_item_kitchen_fixture, kitchenList)
        recyclerView = view.findViewById(R.id.list_kitchen_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(MainActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = itemArrayAdapter

        val kitchenString = arguments!!.getString("myKitchenString")

        val jsonArrayKitchen = JSONArray(kitchenString)

        val prefMgr = SharedPreferenceManager(requireContext())
        prefMgr.setMyKey("kitchenString",kitchenString)
        val newJSON = JSONArray(prefMgr.getMyKey("kitchenFixtureStates"))
        val intent = Intent("kitchen-switch-message")
        for (i in 0 until jsonArrayKitchen.length()) {
            val item = jsonArrayKitchen.getString(i)
            if(newJSON.getString(i) == "off") {
                intent.putExtra("state","false")
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            } else {
                intent.putExtra("state","true")
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            }

            kitchenList.add(KitchenFixtureItem(item,newJSON.getString(i)))
            val kitchenItemArrayAdapter = KitchenFixtureArrayAdapter(R.layout.list_item_kitchen_fixture, kitchenList)
            recyclerView = view!!.findViewById(R.id.list_kitchen_fixtures)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = kitchenItemArrayAdapter
        }

        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchAction,
                IntentFilter("kitchen-switch-message")
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                manualAction,
                IntentFilter("manual-kitchen-switch-message")
            )
        } catch(npe:NullPointerException) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchAction,
                IntentFilter("kitchen-switch-message")
            )

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                manualAction,
                IntentFilter("manual-kitchen-switch-message")
            )
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        addTitle()
    }

    fun addTitle() {
        val prefMgr = SharedPreferenceManager(requireContext())
        prefMgr.setMyKey("title",getString(R.string.kitchen_fixtures_title))
    }

    private val switchAction: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val livingSwitch: Switch = view!!.findViewById(R.id.kitchenSwitch)
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
            val livingSwitch: Switch = view!!.findViewById(R.id.kitchen)
            val pos = intent!!.getStringExtra("position")
            val state = intent!!.getStringExtra("state")

            if(state=="on") {
                livingSwitch.isChecked = true
            }
            if(state=="off") {
                livingSwitch.isChecked = false
            }

            val prefMgr = SharedPreferenceManager(requireContext())
            val newJSONList = JSONArray(prefMgr.getMyKey("kitchenFixtureStates"))
            newJSONList.put(pos.toInt(),state)
            prefMgr.setMyKey("kitchenFixtureStates", newJSONList.toString())
            setKitchenSwitch(state,pos.toInt())
        }
    }

    fun setKitchenSwitch(state: String,position: Int) {
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
