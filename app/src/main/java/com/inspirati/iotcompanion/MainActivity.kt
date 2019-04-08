package com.inspirati.iotcompanion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.inspirati.iotcompanion.viewController.*
import org.json.JSONArray
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import com.inspirati.iotcompanion.model.*
import com.inspirati.iotcompanion.viewController.BedFixturesFragment
import kotlinx.android.synthetic.main.app_bar_menu.*


class MainActivity : AppCompatActivity(), WeatherIntentServiceResultReceiver.Receiver {

    var adjustAirConditioner = "AdjustAirConditioner"

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        when (resultCode) {
            WeatherIntentService.STATUS_RUNNING -> {

            }

            WeatherIntentService.STATUS_FINISHED -> {
                val myResult = resultData.getString("response")
                val codeRec = resultData.getString("responseCode")
                if (codeRec =="1100") {
                   try {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(adjustAirConditioner)
                            .putExtra("the_temp",myResult))
                   } catch(e:Exception) {
                    }
                }
            }

            WeatherIntentService.STATUS_ERROR -> {
                val error = resultData.getString(Intent.EXTRA_TEXT)
            }
        }
    }

    var GETROOM_STATUS_UPDATE = "getAllRooms"

    lateinit var homeBtn:MenuItem
    lateinit var bedroomBtn:MenuItem
    lateinit var kitchenBtn:MenuItem
    lateinit var livingBtn:MenuItem
    var HOME_NAVIGATION = "HomeNavigation"
    var BEDROOM_NAVIGATION = "BedroomNavigation"
    var KITCHEN_NAVIGATION = "KitchenNavigation"
    var LIVING_NAVIGATION = "LivingNavigation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        this.supportActionBar!!.title = "Rooms"


        LocalBroadcastManager.getInstance(this).registerReceiver(homeMessageReceiver,
            IntentFilter(HOME_NAVIGATION)
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(bedroomMessageReceiver,
            IntentFilter(BEDROOM_NAVIGATION)
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(kitchenMessageReceiver,
            IntentFilter(KITCHEN_NAVIGATION)
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(livingMessageReceiver,
            IntentFilter(LIVING_NAVIGATION)
        )

        val prefManager = SharedPreferenceManager(applicationContext)

        if(prefManager.getMyKey("bedroomFixtureStates").isNullOrEmpty() ) {
            val initialState = "['off','off','off']"
            val bedSwitchArray = JSONArray(initialState)
            val kitchenSwitchArray = JSONArray(initialState)
            val livingSwitchArray = JSONArray(initialState)
            prefManager.setMyKey("bedroomFixtureStates",bedSwitchArray.toString())
            prefManager.setMyKey("kitchenFixtureStates",kitchenSwitchArray.toString())
            prefManager.setMyKey("livingFixtureStates",livingSwitchArray.toString())
        }

        fetchList()
        mReceiver = WeatherIntentServiceResultReceiver(Handler())
        mReceiver!!.setReceiver(this)
        val msgIntent = Intent(this, WeatherIntentService::class.java)
        msgIntent.putExtra("receiver",mReceiver)
        msgIntent.putExtra("requestId","1101")
        this.startService(msgIntent)
    }

    private var mReceiver: WeatherIntentServiceResultReceiver? = null

    private var homeMessageReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            goHome()
        }
    }

    private var bedroomMessageReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            goToBedRoom()
        }
    }

    private var kitchenMessageReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            goToKitchen()
        }
    }

    private var livingMessageReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            goToLiving()
        }
    }

    fun fetchList() {
        this.supportActionBar!!.title = getString(R.string.rooms_title)

        val args = Bundle()
        args.putString("fetchInfo","yes")
        HomeFragment().arguments = args

        val manager = supportFragmentManager
        val ft = manager.beginTransaction()
        ft.addToBackStack("home")
        ft.replace(R.id.content_frame, HomeFragment(), "home")
        ft.commitAllowingStateLoss()
    }

    fun goHome() {
        this.supportActionBar!!.title = getString(R.string.rooms_title)
        val manager = supportFragmentManager
        val ft = manager.beginTransaction()
            ft.addToBackStack("home")
        ft.replace(R.id.content_frame, HomeFragment(), "home")
        ft.commitAllowingStateLoss()
    }

    fun goToBedRoom() {
        val preferenceManager = SharedPreferenceManager(this)

        addFragment(BedFixturesFragment(), preferenceManager.getMyKey("bedroomData").toString(),
            getString(R.string.myBedString)
            , getString(R.string.bedroom_fixtures_title),getString(R.string.bedroomfixtures), true)
    }

    fun goToKitchen() {
        val prefManager = SharedPreferenceManager(this)
        addFragment(KitchenFixturesFragment(), prefManager.getMyKey("kitchenData").toString(),getString(R.string.myKitchenString)
            , getString(R.string.kitchen_fixtures_title),getString(R.string.kitchenfixtures),true)
    }

    fun goToLiving() {
        val preferenceMgr = SharedPreferenceManager(this)
        addFragment(LivingFixturesFragment(), preferenceMgr.getMyKey("livingRoomData").toString(),
            getString(R.string.myLivingRoomString),
            getString(R.string.living_room_fixtures_title),getString(R.string.livingroomfixtures),true)
    }

    fun hideBedroomMenu() {
        homeBtn.isVisible = true
        bedroomBtn.isVisible = false
        kitchenBtn.isVisible = true
        livingBtn.isVisible = true
    }

    fun hideKitchenMenu() {
        homeBtn.isVisible = true
        bedroomBtn.isVisible = true
        kitchenBtn.isVisible = false
        livingBtn.isVisible = true
    }

    fun hideLivingMenu() {
        homeBtn.isVisible = true
        bedroomBtn.isVisible = true
        kitchenBtn.isVisible = true
        livingBtn.isVisible = false
    }

    fun hideOptionsMenu() {
        bedroomBtn.isVisible = false
        kitchenBtn.isVisible = false
        livingBtn.isVisible = false
    }

    private fun addFragment(fragment: Fragment,myObject: String, roomString: String,
                            screenTitle: String, tag: String, addToBackStack: Boolean) {
        this.supportActionBar!!.title = screenTitle

        val args = Bundle()
        args.putString(roomString,myObject)
        fragment.arguments = args

        when(screenTitle) {
            getString(R.string.bedroom_fixtures_title) -> hideBedroomMenu()
            getString(R.string.kitchen_fixtures_title) -> hideKitchenMenu()
            getString(R.string.living_room_fixtures_title) -> hideLivingMenu()
        }
        val manager = supportFragmentManager
        val ft = manager.beginTransaction()

        if (addToBackStack) {
            ft.addToBackStack(tag)
        }
        ft.replace(R.id.content_frame, fragment, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        homeBtn = menu.findItem(R.id.my_home)
        bedroomBtn = menu.findItem(R.id.my_bedroom_screen)
        kitchenBtn = menu.findItem(R.id.my_kitchen_screen)
        livingBtn = menu.findItem(R.id.my_living_screen)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.my_home -> goHome()
            R.id.my_bedroom_screen -> goToBedRoom()
            R.id.my_kitchen_screen -> goToKitchen()
            R.id.my_living_screen -> goToLiving()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }


    override fun onBackPressed() {
      super.onBackPressed()
        val prefMgr = SharedPreferenceManager(this)
        this.supportActionBar!!.title = prefMgr.getMyKey("title")
    }
}
