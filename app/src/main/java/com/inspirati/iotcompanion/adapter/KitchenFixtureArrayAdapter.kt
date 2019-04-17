package com.inspirati.iotcompanion.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.model.KitchenFixtureItem
import com.inspirati.iotcompanion.viewController.HomeActivity


class KitchenFixtureArrayAdapter(layoutId:Int, fixturesList:ArrayList<KitchenFixtureItem>):
    RecyclerView.Adapter<KitchenFixtureArrayAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return fixturesList.size
    }


    private var listItemLayout:Int = 0
    private val fixturesList:ArrayList<KitchenFixtureItem>

    init{
        listItemLayout = layoutId
        this.fixturesList = fixturesList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(listItemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, listPosition:Int) {
        val kitchen = holder.kitchen
        val status = holder.status
        kitchen.text = fixturesList[listPosition].fixture
        status.text = fixturesList[listPosition].status
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var kitchen: TextView
        var status: TextView
        init{
            itemView.setOnClickListener(this)
            kitchen = itemView.findViewById(R.id.kitchen)
            status = itemView.findViewById(R.id.kitchen_status)
        }

        override fun onClick(view: View) {
            val intent = Intent("switch-kitchen-message")
            intent.putExtra("itemPosition",layoutPosition.toString())
            LocalBroadcastManager.getInstance(HomeActivity()).sendBroadcast(intent)
        }
    }

}