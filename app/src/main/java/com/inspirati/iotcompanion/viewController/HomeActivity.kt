package com.inspirati.iotcompanion.viewController

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.text.Html
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.app_bar_menu.*
import com.couchbase.lite.*
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.WeatherIntentService
import com.inspirati.iotcompanion.WeatherIntentServiceResultReceiver
import com.inspirati.iotcompanion.model.GetRooms
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.NullPointerException

class HomeActivity : AppCompatActivity(), WeatherIntentServiceResultReceiver.Receiver {

    var adjustAirConditioner = "AdjustAirConditioner"
    private lateinit var apiInterface: APIInterface
    lateinit var jsonArrayBed: JSONArray
    lateinit var jsonArrayKitchen: JSONArray
    lateinit var jsonArrayLiving: JSONArray
    var BEDLIST_STATUS_UPDATE = "populateBedList"
    var KITCHENLIST_STATUS_UPDATE = "populateKitchenList"
    var LIVINGLIST_STATUS_UPDATE = "populateLivingList"
    var myBedroomData = ""
    var myKitchenData = ""
    var myLivingRoomData = ""
    var initBedArray = ""
    var initLivingArray = ""
    var initKitArray = ""
    var initBedData = ""
    var initKitData = ""
    var initLivingData = ""

    lateinit var database:Database
    lateinit var mutableDoc: MutableDocument

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        when (resultCode) {
            WeatherIntentService.STATUS_RUNNING -> {

            }

            WeatherIntentService.STATUS_FINISHED -> {
                val myResult = resultData.getString("response")
                val codeRec = resultData.getString("responseCode")
                if (codeRec =="1100") {
                    try {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(
                            Intent(adjustAirConditioner)
                                .putExtra("the_temp",myResult))
                    } catch(e:Exception) {
                    }
                }
            }

            WeatherIntentService.STATUS_ERROR -> {
                //val error = resultData.getString(Intent.EXTRA_TEXT)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        this.supportActionBar!!.title = "Rooms"
        apiInterface = APIClient.getClient().create(APIInterface::class.java)


        tabLayout = findViewById(R.id.tabs_rooms)
        viewPager = findViewById(R.id.viewpager_rooms)

        viewPager.adapter = MyAdapter(supportFragmentManager)

        tabLayout.post {
            tabLayout.setupWithViewPager(
                viewPager
            )
        }
        viewPager.offscreenPageLimit = 2

        val initialState = "['off','off','off']"
        val initialLivingState = "['off','off']"
        val myBedSwitchArray = JSONArray(initialState)
        val myKitchenSwitchArray = JSONArray(initialState)
        val myLivingSwitchArray = JSONArray(initialLivingState)


        val config = DatabaseConfiguration(applicationContext)

        database = Database("iotDB", config)

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("bedroomFixtureStates").isNot(Expression.string(null)))
        val result = query.execute()

        if(result.allResults().isNullOrEmpty()){
                mutableDoc = MutableDocument()
                    .setString("bedroomFixtureStates", myBedSwitchArray.toString())
                    .setString("kitchenFixtureStates", myKitchenSwitchArray.toString())
                    .setString("livingFixtureStates", myLivingSwitchArray.toString())
            database.save(mutableDoc)
            mutableDoc = database.getDocument(mutableDoc.id).toMutable()
            initBedArray = myBedSwitchArray.toString()
            initKitArray = myKitchenSwitchArray.toString()
            initLivingArray = myLivingSwitchArray.toString()
        } else {
            val configCtx = DatabaseConfiguration(applicationContext)

            database = Database("iotDB", configCtx)

            val queryBedData = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                    SelectResult.property(getString(R.string.bedroomData)))
                .from(DataSource.database(database))
                .where(Expression.property(getString(R.string.bedroomData))
                    .isNot(Expression.string(null)))

            val queryKitData = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                    SelectResult.property(getString(R.string.kitchenData)))
                .from(DataSource.database(database))
                .where(Expression.property(getString(R.string.kitchenData))
                    .isNot(Expression.string(null)))

            val queryLivData = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                    SelectResult.property(getString(R.string.livingRoomData)))
                .from(DataSource.database(database))
                .where(Expression.property(getString(R.string.livingRoomData))
                    .isNot(Expression.string(null)))
            try {
                val resultBedData = queryBedData.execute()
                val resultKitData = queryKitData.execute()
                val resultLivData = queryLivData.execute()
                initBedData = resultBedData.allResults().last().getString(getString(R.string.bedroomData))
                initKitData = resultKitData.allResults().last().getString(getString(R.string.kitchenData))
                initLivingData = resultLivData.allResults().last().getString(getString(R.string.livingRoomData))
            } catch(npe:NullPointerException) {
                Log.i("COUCHDBLOCAL",npe.message)
            }
        }

        mReceiver = WeatherIntentServiceResultReceiver(Handler())
        mReceiver!!.setReceiver(this)
        val msgIntent = Intent(this, WeatherIntentService::class.java)
        msgIntent.putExtra("receiver",mReceiver)
        msgIntent.putExtra("requestId","1101")
        this.startService(msgIntent)
        getAllRooms()
    }

    private var mReceiver: WeatherIntentServiceResultReceiver? = null

    fun getAllRooms () {
        val call = apiInterface.allRooms

            call.enqueue(object : Callback<GetRooms> {
                override fun onResponse(call: Call<GetRooms>, response: Response<GetRooms>) {

                    if (response.isSuccessful) {
                        try {
                            val bed = response.body()?.rooms?.bedroom?.fixtures
                            val kitchen = response.body()?.rooms?.kitchen?.fixtures
                            val living = response.body()?.rooms?.livingRoom?.fixtures

                            jsonArrayBed = JSONArray(bed)
                            jsonArrayKitchen = JSONArray(kitchen)
                            jsonArrayLiving = JSONArray(living)

                            mutableDoc = MutableDocument()
                                .setString(getString(R.string.bedroomData),jsonArrayBed.toString())
                                .setString(getString(R.string.kitchenData),jsonArrayKitchen.toString())
                                .setString(getString(R.string.livingRoomData),jsonArrayLiving.toString())
                            database.save(mutableDoc)

                            mutableDoc = database.getDocument(mutableDoc.id).toMutable()
                            val document = database.getDocument(mutableDoc.id)

                            myBedroomData = document.getString(getString(R.string.bedroomData))
                            val intent = Intent("load-the-list")
                            intent.putExtra("bedStates",initBedArray)
                            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(intent)

                            myKitchenData = document.getString(getString(R.string.kitchenData))
                            val kitchentIntent = Intent("load-kitchen-list")
                            intent.putExtra("kitStates",initKitArray)
                            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(kitchentIntent)

                            myLivingRoomData = document.getString(getString(R.string.livingRoomData))
                            val livingRoomIntent = Intent("load-living-list")
                            intent.putExtra("livingStates",initLivingArray)
                            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(livingRoomIntent)

                        } catch (exp: JSONException) {
                            Toast.makeText(this@HomeActivity, exp.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<GetRooms>, t: Throwable) {
                    if(initBedData.isEmpty()) {
                        Toast.makeText(this@HomeActivity, getString(R.string.firstTimeMessage),
                            Toast.LENGTH_LONG).show()
                    } else {
                        try {
                            val bedRoomData = initBedData
                            val kitchen = initKitData
                            val living = initLivingData

                            myBedroomData = bedRoomData
                            val intent = Intent("load-the-list")
                            intent.putExtra("bedStates",initBedArray)
                            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(intent)

                            myKitchenData = kitchen
                            val kitchentIntent = Intent("load-kitchen-list")
                            intent.putExtra("kitStates",initKitArray)
                            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(kitchentIntent)

                            myLivingRoomData = living
                            val livingRoomIntent = Intent("load-living-list")
                            intent.putExtra("livingStates",initLivingArray)
                            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(livingRoomIntent)

                                    jsonArrayBed = JSONArray(bedRoomData)
                                    jsonArrayKitchen = JSONArray(kitchen)
                                    jsonArrayLiving = JSONArray(living)

                                    /*if (jsonArrayBed.length() > 0) {
                                        MyBedroomFragment().bedList.clear()
                                        try {
                                            LocalBroadcastManager.getInstance(this@HomeActivity)
                                                .sendBroadcast(Intent(BEDLIST_STATUS_UPDATE))
                                            LocalBroadcastManager.getInstance(this@HomeActivity)
                                                .sendBroadcast(Intent(KITCHENLIST_STATUS_UPDATE))
                                            LocalBroadcastManager.getInstance(this@HomeActivity)
                                                .sendBroadcast(Intent(LIVINGLIST_STATUS_UPDATE))
                                        } catch (e: Exception) {
                                        }
                                    }*/
                        } catch(npe:NullPointerException){
                            Toast.makeText(this@HomeActivity, getString(R.string.localDBError),
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
    }

    internal inner class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return MyBedroomFragment()
                1 -> return MyKitchenFragment()
                2 -> return MyLivingRoomFragment()
            }
            return null
        }

        override fun getCount(): Int {
            return int_items
        }

        override fun getPageTitle(position: Int): CharSequence? {

            when (position) {
                0 -> return "Bedroom"
                1 -> return "Kitchen"
                2 -> return "Living Room"
            }
            return null
        }
    }

    companion object {
        lateinit var tabLayout: TabLayout
        lateinit var viewPager: ViewPager
        var int_items = 3
    }

    override fun onBackPressed() {

        val fragMgr = supportFragmentManager

        val count = fragMgr.backStackEntryCount

        if(count==0) {
            val builder = AlertDialog.Builder(this@HomeActivity)

            builder.setTitle("IoT Companion App")

            builder.setMessage("Do you want to exit?")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPositiveButton(
                    Html.fromHtml("<font color='#00695c'>YES</font>", 0)
                ) { _, _ ->  finish()}
            } else {
                builder.setPositiveButton("YES"
                ) { _, _ ->  finish()}
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setNegativeButton(Html.fromHtml("<font color='#dd2c00'>NO</font>", 0)
                ) { _, _ -> Toast.makeText(applicationContext,resources.getString(R.string.keepControlling),
                    Toast.LENGTH_SHORT).show()}
            } else {
                builder.setNegativeButton("NO"
                ) { _, _ -> Toast.makeText(applicationContext,resources.getString(R.string.keepControlling),
                    Toast.LENGTH_SHORT).show()}
            }

            builder.setNeutralButton("Cancel"){_,_ ->
                Toast.makeText(applicationContext,resources.getString(R.string.keepControlling),
                    Toast.LENGTH_SHORT).show()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}
