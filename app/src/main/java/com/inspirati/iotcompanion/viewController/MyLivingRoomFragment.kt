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
import com.inspirati.iotcompanion.adapter.LivingFixtureArrayAdapter
import com.inspirati.iotcompanion.model.LivingFixtureItem
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException


class MyLivingRoomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiInterface: APIInterface
    lateinit var database:Database
    lateinit var mutableDoc: MutableDocument
    private lateinit var livingSwitchStates: String
    private lateinit var myLivstates: String
    private lateinit var myLivingstates: String

    var livingRoomList: ArrayList<LivingFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_living_room, container, false)

        livingRoomList.clear()

        val config = DatabaseConfiguration(requireContext())
        database = Database("iotDB", config)

        val itemArrayAdapter = LivingFixtureArrayAdapter(R.layout.list_item_living_fixture, livingRoomList)
        recyclerView = view.findViewById(R.id.list_living_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = itemArrayAdapter

        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                loadLivingListReceiver,
                IntentFilter("load-living-list")
            )
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                switchMessageReceiver,
                IntentFilter("switch-living-message")
            )
        } catch(npe: NullPointerException) {
        }
        return view
    }

    private val loadLivingListReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            try {
                myLivingstates = intent!!.getStringExtra("livingStates")
            } catch (e: Exception) {
                myLivingstates = "['off','off']"
            }
            populateLivingRoomList()
        }
    }

    fun populateLivingRoomList() {

        var query = QueryBuilder
            .select(SelectResult.expression(Meta.id),
                SelectResult.property(getString(R.string.livingRoomData)))
            .from(DataSource.database(database))
            .where(Expression.property(getString(R.string.livingRoomData))
                .isNot(Expression.string(null)))

        val queryStates = QueryBuilder
            .select(SelectResult.expression(Meta.id),
                SelectResult.property("livingFixtureStates"))
            .from(DataSource.database(database))
            .where(Expression.property("livingFixtureStates")
                .isNot(Expression.string(null)))
        try
        {
            val rs = query.execute()

            val initLivingString = rs.allResults().last().getString(getString(R.string.livingRoomData))

            val jsonArrayLiving = JSONArray(initLivingString)

            mutableDoc = MutableDocument()
                .setString("livingString", initLivingString)
            database.save(mutableDoc)
            try {
                val rsStates = queryStates.execute()
                try {
                    livingSwitchStates = rsStates.allResults().last().getString("livingFixtureStates")
                } catch(e:Exception) {
                    livingSwitchStates = myLivingstates
                }

                val newJSON = JSONArray(livingSwitchStates)
                for (i in 0 until jsonArrayLiving.length()) {
                    val item = jsonArrayLiving.getString(i)

                    livingRoomList.add(LivingFixtureItem(item, newJSON.getString(i)))
                    val newItemArrayAdapter =
                        LivingFixtureArrayAdapter(R.layout.list_item_living_fixture, livingRoomList)
                    recyclerView = view!!.findViewById(R.id.list_living_fixtures)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.itemAnimator = DefaultItemAnimator()
                    recyclerView.adapter = newItemArrayAdapter
                }
            } catch (e:CouchbaseLiteException){}
        }
        catch (e:CouchbaseLiteException) {
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
                    ) { _, _ ->  setLivingSwitch("on",pos!!.toInt())}
                } else {
                    setPositiveButton("ON"
                    ) { _, _ ->  setLivingSwitch("on",pos!!.toInt())}
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setNegativeButton(Html.fromHtml("<font color='#dd2c00'>OFF</font>", 0)
                    ) { _, _ ->  setLivingSwitch("off",pos!!.toInt())}
                } else {
                    setNegativeButton("OFF"
                    ) { _, _ ->  setLivingSwitch("off",pos!!.toInt())}
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
        livingRoomList.clear()
        val livingSwitchArray = JSONArray(livingSwitchStates)
        livingSwitchArray.put(position,state)
        Log.i("fixtureslivingstr",livingSwitchArray.toString())
        mutableDoc = MutableDocument()
            .setString("livingFixtureStates", livingSwitchArray.toString())
        database.save(mutableDoc)
        mutableDoc = database.getDocument(mutableDoc.id).toMutable()
        livingSwitchStates = livingSwitchArray.toString()
        populateLivingRoomList()
    }

    fun setLivingSwitch(state: String,position: Int) {

        apiInterface = APIClient.getClient().create(APIInterface::class.java)
        var call = apiInterface.turnOnLivingLightOne()
        when (position){
            0 -> {
                if(state=="on") {
                    call = apiInterface.turnOnLivingLightOne()
                }
                if(state=="off") {
                    call = apiInterface.turnOffLivingLightOne()
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
                    call = apiInterface.turnOnLivingLightTwo()
                }
                if(state=="off") {
                    call = apiInterface.turnOffLivingLightTwo()
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
