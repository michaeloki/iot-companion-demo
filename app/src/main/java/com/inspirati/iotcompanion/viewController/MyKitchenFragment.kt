package com.inspirati.iotcompanion.viewController


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.couchbase.lite.*

import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.adapter.KitchenFixtureArrayAdapter
import com.inspirati.iotcompanion.model.KitchenFixtureItem
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException


class MyKitchenFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiInterface: APIInterface
    lateinit var database:Database
    lateinit var mutableDoc: MutableDocument
    private lateinit var kitchenSwitchStates: String
    private lateinit var myKitstates: String

    var kitchenList: ArrayList<KitchenFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_kitchen, container, false)

        kitchenList.clear()

        val config = DatabaseConfiguration(requireContext())
        database = Database("iotDB", config)

        val itemArrayAdapter = KitchenFixtureArrayAdapter(R.layout.list_item_kitchen_fixture, kitchenList)
        recyclerView = view.findViewById(R.id.list_kitchen_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = itemArrayAdapter

        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                loadKitchenListReceiver,
                IntentFilter("load-kitchen-list")
            )
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchMessageReceiver,
                IntentFilter("switch-kitchen-message")
            )
        } catch(npe: NullPointerException) {
        }
        return view
    }

    private val loadKitchenListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                myKitstates = intent!!.getStringExtra("kitStates")
            } catch (e:Exception) {
                myKitstates = "['off','off','off']"
            }
            populateKitchenList()
        }
    }

    fun populateKitchenList() {

        var query = QueryBuilder
            .select(SelectResult.expression(Meta.id),
                SelectResult.property(getString(R.string.kitchenData)))
            .from(DataSource.database(database))
            .where(Expression.property(getString(R.string.kitchenData))
                .isNot(Expression.string(null)))

        var queryStates = QueryBuilder
            .select(SelectResult.expression(Meta.id),
                SelectResult.property("kitchenFixtureStates"))
            .from(DataSource.database(database))
            .where(Expression.property("kitchenFixtureStates")
                .isNot(Expression.string(null)))
        try
        {
            val rs = query.execute()

            val initkitchenString = rs.allResults().last().getString(getString(R.string.kitchenData))

            val jsonArrayKit = JSONArray(initkitchenString)

            mutableDoc = MutableDocument()
                .setString("kitchenString", initkitchenString)
            database.save(mutableDoc)

            try {
                val rsStates = queryStates.execute()
                kitchenSwitchStates = rsStates.allResults().last().getString("kitchenFixtureStates")
            } catch(e: Exception) {
                kitchenSwitchStates = myKitstates
            }
            val newJSON = JSONArray(kitchenSwitchStates)
            for (i in 0 until jsonArrayKit.length()) {
                val item = jsonArrayKit.getString(i)

                kitchenList.add(KitchenFixtureItem(item,newJSON.getString(i)))
                val newItemArrayAdapter = KitchenFixtureArrayAdapter(R.layout.list_item_kitchen_fixture, kitchenList)
                recyclerView = view!!.findViewById(R.id.list_kitchen_fixtures)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.itemAnimator = DefaultItemAnimator()
                recyclerView.adapter = newItemArrayAdapter
            }

        }
        catch (e:CouchbaseLiteException) {
            Log.e("savmsss3", e.localizedMessage)
        }
    }

    private val switchMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pos = intent?.getStringExtra("itemPosition")
            val builder = AlertDialog.Builder(requireContext()).apply {
                setTitle("Switch")
                setMessage("Make your choice")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setPositiveButton(
                        Html.fromHtml("<font color='#00695c'>ON</font>", 0)
                    ) { _, _ ->  setKitSwitch("on",pos!!.toInt())}
                } else {
                    setPositiveButton("ON"
                    ) { _, _ ->  setKitSwitch("on",pos!!.toInt())}
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setNegativeButton(Html.fromHtml("<font color='#dd2c00'>OFF</font>", 0)
                    ) { _, _ ->  setKitSwitch("off",pos!!.toInt())}
                } else {
                    setNegativeButton("OFF"
                    ) { _, _ ->  setKitSwitch("off",pos!!.toInt())}
                }
                setNeutralButton("Ignore") {
                        _, _ ->
                }
                setCancelable(true)
            }
            builder.create()
            builder.show()
        }
    }


    fun updateStates(state: String,position: Int) {
        kitchenList.clear()
        val kitSwitchArray = JSONArray(kitchenSwitchStates)
        kitSwitchArray.put(position,state)

        mutableDoc = MutableDocument()
            .setString("kitchenFixtureStates", kitSwitchArray.toString())
        database.save(mutableDoc)
        mutableDoc = database.getDocument(mutableDoc.id).toMutable()
        kitchenSwitchStates = kitSwitchArray.toString()
        populateKitchenList()
    }

    fun setKitSwitch(state: String,position: Int) {

        apiInterface = APIClient.getClient().create(APIInterface::class.java)
        var call = apiInterface.turnOnKitchenLightOne()
        when (position){
            0 -> {
                if(state=="on") {
                    call = apiInterface.turnOnKitchenLightOne()
                }
                if(state=="off") {
                    call = apiInterface.turnOffKitchenLightOne()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        updateStates(state,position)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        updateStates(state,position)
                        Toast.makeText(requireContext(), getString(R.string.successMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
            1 -> {
                if(state=="on") {
                    call = apiInterface.turnOnKitchenMusic()
                }
                if(state=="off") {
                    call = apiInterface.turnOffKitchenMusic()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        updateStates(state,position)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        updateStates(state,position)
                        Toast.makeText(requireContext(), getString(R.string.successMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
            2 -> {
                if(state=="on") {
                    call = apiInterface.turnOnKitchenSlowCooker()
                }
                if(state=="off") {
                    call = apiInterface.turnOffKitchenSlowCooker()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        updateStates(state,position)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        updateStates(state,position)
                        Toast.makeText(requireContext(), getString(R.string.successMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
