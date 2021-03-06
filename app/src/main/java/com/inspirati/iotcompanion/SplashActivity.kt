package com.inspirati.iotcompanion


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.inspirati.iotcompanion.viewController.HomeActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val myThread = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(1500)
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
        myThread.start()
    }
}
