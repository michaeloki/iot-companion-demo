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
import com.inspirati.iotcompanion.adapter.BedFixtureArrayAdapter
import com.inspirati.iotcompanion.model.BedFixtureItem
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException


class MyBedroomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiInterface: APIInterface
    lateinit var database:Database
    lateinit var mutableDoc: MutableDocument
    private lateinit var bedroomSwitchStates: String

    var bedList: ArrayList<BedFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_bedroom, container, false)

        bedList.clear()

        val config = DatabaseConfiguration(requireContext())
        database = Database("iotDB", config)

        val itemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
        recyclerView = view.findViewById(R.id.list_bed_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = itemArrayAdapter

        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                loadListReceiver,
                IntentFilter("load-the-list")
            )
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchMessageReceiver,
                IntentFilter("switch-message")
            )
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                adjustTheAC,
                IntentFilter(HomeActivity().adjustAirConditioner)
            )
        } catch(npe: NullPointerException) {

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                adjustTheAC,
                IntentFilter(HomeActivity().adjustAirConditioner)
            )
        }
        return view
    }

    private val loadListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            populateBedroomList()
        }
    }

    fun populateBedroomList() {

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id),
                SelectResult.property(getString(R.string.bedroomData)))
            //SelectResult.property("bedroomFixtureStates"))
            .from(DataSource.database(database))
            .where(Expression.property(getString(R.string.bedroomData))
                .isNot(Expression.string(null)))
        try
        {
            val rs = query.execute()

            val initbedString = rs.allResults().last().getString(getString(R.string.bedroomData))

            val jsonArrayBed = JSONArray(initbedString)

            mutableDoc = MutableDocument()
                .setString("bedString", initbedString)
            database.save(mutableDoc)

            val queryStates = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                    SelectResult.property("bedroomFixtureStates"))
                .from(DataSource.database(database))
                .where(Expression.property("bedroomFixtureStates")
                    .isNot(Expression.string(null)))
            val rsStates = queryStates.execute()

            bedroomSwitchStates = rsStates.allResults().last().getString("bedroomFixtureStates")

            val newJSON = JSONArray(bedroomSwitchStates)
            for (i in 0 until jsonArrayBed.length()) {
                val item = jsonArrayBed.getString(i)
                val queryTemp = QueryBuilder
                    .select(SelectResult.expression(Meta.id),
                        SelectResult.property("the_temp"))
                    .from(DataSource.database(database))
                    .where(Expression.property("the_temp")
                        .isNot(Expression.string(null)))
                var temp = ""
                try {
                    val rsTemp = queryTemp.execute()
                    temp = rsTemp.allResults().last().getString("the_temp")
                } catch(e:Exception) {
                }
                bedList.add(BedFixtureItem(item,newJSON.getString(i),
                    temp))
                val newItemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
                recyclerView = view!!.findViewById(R.id.list_bed_fixtures)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.itemAnimator = DefaultItemAnimator()
                recyclerView.adapter = newItemArrayAdapter
            }
        }
        catch (e:CouchbaseLiteException) {
            Log.e("couchdbException", e.localizedMessage)
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
                    ) { _, _ ->  setSwitch("on",pos!!.toInt())}
                } else {
                    setPositiveButton("ON"
                    ) { _, _ ->  setSwitch("on",pos!!.toInt())}
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setNegativeButton(Html.fromHtml("<font color='#dd2c00'>OFF</font>", 0)
                    ) { _, _ ->  setSwitch("off",pos!!.toInt())}
                } else {
                    setNegativeButton("OFF"
                    ) { _, _ ->  setSwitch("off",pos!!.toInt())}
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

    private val adjustTheAC = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val temp = intent?.getStringExtra("the_temp")
            if(temp?.toDouble()!! >= 25.00) {
                try {
                    mutableDoc = MutableDocument()
                        .setString("the_temp", temp.substring(0,5))
                    database.save(mutableDoc)
                    mutableDoc = database.getDocument(mutableDoc.id).toMutable()

                    val document = database.getDocument(mutableDoc.id)

                    val newJSON = JSONArray(document.getString(getString(R.string.bedroomData)))
                    newJSON.remove(2)
                    newJSON.put(2, "AC $temp.substring(0,5)+°")
                    val bedroomState = JSONArray(document.getString("bedroomFixtureStates"))
                    bedroomState.remove(2)
                    if (bedroomState.getString(2) == "off") {
                        bedroomState.put(2, "on")
                        mutableDoc = MutableDocument()
                            .setString("bedroomFixtureStates", bedroomState.toString())
                        database.save(mutableDoc)
                        mutableDoc = database.getDocument(mutableDoc.id).toMutable()
                    }
                    mutableDoc = MutableDocument()
                        .setString("bedroomData", newJSON.toString())
                    database.save(mutableDoc)
                    mutableDoc = database.getDocument(mutableDoc.id).toMutable()
                } catch (exp: Exception) {}
            } else {
                try {

                    mutableDoc = MutableDocument()
                        .setString("the_temp", temp.toString())
                    database.save(mutableDoc)
                    mutableDoc = database.getDocument(mutableDoc.id).toMutable()

                    val document = database.getDocument(mutableDoc.id)

                    val newJSON = JSONArray(document.getString(getString(R.string.bedroomData)))
                    newJSON.remove(2)
                    newJSON.put(2, "AC $temp.substring(0,5)+°")
                    val bedroomState = JSONArray(document.getString("bedroomFixtureStates"))
                    bedroomState.remove(2)
                    if (bedroomState.getString(2) == "on") {
                        bedroomState.put(2, "off")
                        mutableDoc = MutableDocument()
                            .setString("bedroomFixtureStates", bedroomState.toString())
                        database.save(mutableDoc)
                        mutableDoc = database.getDocument(mutableDoc.id).toMutable()
                    }
                    mutableDoc = MutableDocument()
                        .setString("bedroomData", newJSON.toString())
                    database.save(mutableDoc)
                    mutableDoc = database.getDocument(mutableDoc.id).toMutable()
                } catch (exp: Exception) {}
            }
        }
    }

    fun updateStates(state: String,position: Int) {
        bedList.clear()
        val bedSwitchArray = JSONArray(bedroomSwitchStates)
        bedSwitchArray.put(position,state)

        mutableDoc = MutableDocument()
            .setString("bedroomFixtureStates", bedSwitchArray.toString())
        database.save(mutableDoc)
        mutableDoc = database.getDocument(mutableDoc.id).toMutable()
        bedroomSwitchStates = bedSwitchArray.toString()
        populateBedroomList()
    }

    fun setSwitch(state: String,position: Int) {

        apiInterface = APIClient.getClient().create(APIInterface::class.java)
        var call = apiInterface.turnOnBedLightOne()
        when (position){
            0 -> {
                if(state=="on") {
                    call = apiInterface.turnOnBedLightOne()
                }
                if(state=="off") {
                    call = apiInterface.turnOffBedLightOne()
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
                    call = apiInterface.turnOnBedLightTwo()
                }
                if(state=="off") {
                    call = apiInterface.turnOffBedLightTwo()
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
                    call = apiInterface.turnOnAC()
                }
                if(state=="off") {
                    call = apiInterface.turnOffAC()
                }
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        updateStates(state,position)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        updateStates(state,position)
                        Toast.makeText(requireContext(), getString(R.string.switchOffACMessage), Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
