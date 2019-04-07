package com.inspirati.iotcompanion.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
//import com.inspirati.carsearch.*
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.inspirati.iotcompanion.MainActivity
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.model.BedFixtureItem


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
        val view = LayoutInflater.from(parent.getContext()).inflate(listItemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, listPosition:Int) {
        /*if(listPosition %2 == 1) {
            holder.itemView.setBackgroundColor(Color.parseColor("#BBDEFB"))
        }
        else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"))
        }*/
        val fixture = holder.fixture
        fixture.text = fixturesList[listPosition].fixture
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var fixture: TextView
        var secondLight: TextView
        var airConditioner: TextView
        init{
            itemView.setOnClickListener(this)
            fixture = itemView.findViewById(R.id.fixture)
            secondLight = itemView.findViewById(R.id.secondLight_item)
            airConditioner = itemView.findViewById(R.id.airConditionerCard)
            fixture.setOnClickListener {
                //MainActivity().listItemPosition = layoutPosition

                val intent = Intent("custom-message")
                intent.putExtra("layoutPosition",layoutPosition)
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            }

        }

        override fun onClick(view: View) {
        }
    }

}