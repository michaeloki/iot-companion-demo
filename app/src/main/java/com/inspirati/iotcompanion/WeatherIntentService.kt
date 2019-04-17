package com.inspirati.iotcompanion

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.widget.Toast
import com.inspirati.iotcompanion.model.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class WeatherIntentService : IntentService("WeatherIntentService") {

    private lateinit var apiInterface: WeatherAPIInterface

    override fun onHandleIntent(intent: Intent?) {
        val bundle = Bundle()

        val receiver: ResultReceiver = intent!!.getParcelableExtra("receiver")
        val requestId: String = intent.getStringExtra("requestId")

        apiInterface = WeatherAPIClient.getClient().create(WeatherAPIInterface::class.java)

        if(requestId=="1101") {
            val t = Timer()
            t.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val call = apiInterface.getWeather
                    call.enqueue(object : Callback<Weather> {
                        override fun onFailure(call: Call<Weather>, t: Throwable) {
                            //Toast.makeText(this@WeatherIntentService, t.toString(), Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<Weather>, response: Response<Weather>) {

                            if (response.isSuccessful) {
                                try {
                                    bundle.putString("response", response.body()?.consolidatedWeather?.get(0)?.theTemp.toString())
                                    bundle.putString("responseCode", "1100")
                                    receiver.send(STATUS_FINISHED, bundle)
                                } catch(e:Exception) {
                                }
                            }
                        }
                    })
                }

            }, 0, 180000)
        }

    }


    companion object {
        val STATUS_RUNNING = 0
        val STATUS_FINISHED = 1
        val STATUS_ERROR = 2
    }
}
