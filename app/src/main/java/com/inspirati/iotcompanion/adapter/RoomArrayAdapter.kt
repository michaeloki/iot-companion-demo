package com.inspirati.iotcompanion.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.inspirati.iotcompanion.MainActivity
import com.inspirati.iotcompanion.R
import com.inspirati.iotcompanion.model.RoomItem


class RoomArrayAdapter(layoutId:Int, roomsList:ArrayList<RoomItem>):
    RecyclerView.Adapter<RoomArrayAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return roomsList.size
    }


    private var listItemLayout:Int = 0
    private val roomsList:ArrayList<RoomItem>

    init{
        listItemLayout = layoutId
        this.roomsList = roomsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(listItemLayout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, listPosition:Int) {
        val messageText = holder.messageText
        messageText.text = roomsList[listPosition].messageText
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var messageText: TextView
        init{
            itemView.setOnClickListener(this)
            messageText = itemView.findViewById(R.id.room_item)
            messageText.setOnClickListener {
                val intent = Intent("navigation-message")
                intent.putExtra("layoutPosition",layoutPosition)
                LocalBroadcastManager.getInstance(MainActivity()).sendBroadcast(intent)
            }

        }

        override fun onClick(view: View) {
        }
    }

}