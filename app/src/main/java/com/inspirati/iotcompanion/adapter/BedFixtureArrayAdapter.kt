package com.inspirati.iotcompanion.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.widget.*
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.model.BedFixtureItem
import com.inspirati.iotcompanion.viewController.HomeActivity


class BedFixtureArrayAdapter(layoutId:Int, fixturesList:ArrayList<BedFixtureItem>):
    RecyclerView.Adapter<BedFixtureArrayAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return fixturesList.size
    }

    private var listItemLayout:Int = 0
    private val fixturesList:ArrayList<BedFixtureItem>

    init{
        listItemLayout = layoutId
        this.fixturesList = fixturesList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(listItemLayout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder:ViewHolder, listPosition:Int) {
        val fixture = holder.fixture
        val status = holder.status
        val temp = holder.temp

        if(listPosition!=2) {
            fixture.text = fixturesList[listPosition].fixture
            status.text = fixturesList[listPosition].status
        } else {
            fixture.text = fixturesList[listPosition].fixture
            status.text = fixturesList[listPosition].status
            temp.text = fixturesList[listPosition].temp
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var fixture: TextView
        var status: TextView
        var temp: TextView

        init {
            itemView.setOnClickListener(this)
            fixture = itemView.findViewById(R.id.fixture)
            status = itemView.findViewById(R.id.status)
            temp = itemView.findViewById(R.id.temp)
        }
        override fun onClick(view: View) {
            val intent = Intent("switch-message")
            intent.putExtra("itemPosition",layoutPosition.toString())
            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(intent)
        }
    }
}