package com.craftrom.raiderstatus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@SuppressLint("CustomSplashScreen")
abstract class SplashActivity : AppCompatActivity() {

    companion object {
        private var skipSplash = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition { !skipSplash }
        }

        if (skipSplash) {
            showMainUI(savedInstanceState)
        } else {
            preLoad(savedInstanceState)

        }
    }


    private fun preLoad(savedState: Bundle?) {
        runOnUiThread {
            skipSplash = true
            showMainUI(savedState)
        }
    }


    abstract fun showMainUI(savedInstanceState: Bundle?)
}