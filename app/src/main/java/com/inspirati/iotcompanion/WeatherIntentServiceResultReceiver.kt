package com.inspirati.iotcompanion

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class WeatherIntentServiceResultReceiver(handler: Handler) : ResultReceiver(handler) {
    private var mReceiver: Receiver? = null

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (mReceiver != null) {
            mReceiver!!.onReceiveResult(resultCode, resultData)
        }
    }
}