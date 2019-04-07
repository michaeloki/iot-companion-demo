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
import android.widget.ProgressBar
import android.widget.TextView
import com.inspirati.iotcompanion.MainActivity

import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.adapter.BedFixtureArrayAdapter
import com.inspirati.iotcompanion.model.BedFixtureItem

class BedFixturesFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var myResultTextView: TextView
    private lateinit var statusUpdateBtn : Button


    var bedList: ArrayList<BedFixtureItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bed_fixtures, container, false)

        val itemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
        recyclerView = view.findViewById(R.id.list_bed_fixtures)
        recyclerView.layoutManager = LinearLayoutManager(MainActivity())
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = itemArrayAdapter

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(populateList,
            IntentFilter(MainActivity().BEDLIST_STATUS_UPDATE)
        )

        return view
    }

    private val populateList = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {


            for (i in 0 until MainActivity().jsonArrayBed.length()) {
                val item = MainActivity().jsonArrayBed.getString(i)

                bedList.add(BedFixtureItem(item))
                val itemArrayAdapter = BedFixtureArrayAdapter(R.layout.list_item_bed_fixture, bedList)
                recyclerView = view!!.findViewById(R.id.list_bed_fixtures)
                recyclerView.layoutManager = LinearLayoutManager(getContext())
                recyclerView.itemAnimator = DefaultItemAnimator()
                recyclerView.adapter = itemArrayAdapter
            }

            /*var preferenceManager = PreferenceManager(activity)
            var email = preferenceManager.getMyKey("email")

            if(email == null || email.isEmpty()){
                spinner.isEnabled = false
                spinnerBlood.isEnabled = false
                updateBtn.isEnabled = false
                switchState.isEnabled = false
            } else {
                spinner.isEnabled = true
                spinnerBlood.isEnabled = true
                updateBtn.isEnabled = true
                switchState.isEnabled = true
                updateBtn.setBackgroundResource(R.color.darkBlue)
            }*/
        }
    }

    /*
                                    */
}
