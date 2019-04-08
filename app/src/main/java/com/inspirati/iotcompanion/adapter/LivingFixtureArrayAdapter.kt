package com.inspirati.iotcompanion.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.widget.Switch
import com.inspirati.iotcompanion.MainActivity
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.model.LivingFixtureItem


class LivingFixtureArrayAdapter(layoutId:Int, fixturesList:ArrayList<LivingFixtureItem>):
    RecyclerView.Adapter<LivingFixtureArrayAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return fixturesList.size
    }

    private var listItemLayout:Int = 0
    private val fixturesList:ArrayList<LivingFixtureItem>

    init{
        listItemLayout = layoutId
        this.fixturesList = fixturesList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(listItemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, listPosition:Int) {
        val fixture = holder.living
        val status = holder.status
        fixture.text = fixturesList[listPosition].fixture
        status.text = fixturesList[listPosition].status
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var living: TextView
        var status: TextView
        init{
            itemView.setOnClickListener(this)
            living = itemView.findViewById(R.id.living)
            val livingSwitch: Switch = itemView.findViewById(R.id.livingSwitch)

            livingSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val intent = Intent("manual-living-switch-message")
                    intent.putExtra("state","on")
                    intent.putExtra("position",layoutPosition.toString())
                    LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
                } else {
                    val intent = Intent("manual-living-switch-message")
                    intent.putExtra("state","off")
                    intent.putExtra("position",layoutPosition.toString())
                    LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
                }
            }
            living = itemView.findViewById(R.id.living)
            status = itemView.findViewById(R.id.living_status)
        }

        override fun onClick(view: View) {
        }
    }

}