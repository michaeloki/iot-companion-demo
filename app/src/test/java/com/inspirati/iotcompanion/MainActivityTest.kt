package com.inspirati.iotcompanion

import android.app.LauncherActivity
import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import com.google.common.collect.Range.greaterThan
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowIntent


@RunWith(RobolectricTestRunner::class)
@Config(constants=BuildConfig::class, packageName = "com.inspirati.iotcompanion")

class MainActivityTest {
    var mainActivity:MainActivity= null!!
    var splashActivity:SplashActivity= null!!
    @Before
    fun init(){
        mainActivity = Robolectric.setupActivity(MainActivity::class.java)
    }

    @Test
    fun LauncherActivity() {
        val imageView = splashActivity.findViewById<ImageView>(R.id.logoImage)
        assertNotNull(imageView)
    }

    @Test
    fun checkPageTitle_presentOrNot() {
        assertTrue(mainActivity.getTitle().toString().equals("IOTCompanion"));
    }
}