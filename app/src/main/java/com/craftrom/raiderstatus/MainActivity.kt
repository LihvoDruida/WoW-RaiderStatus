package com.craftrom.raiderstatus

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.craftrom.raiderstatus.core.ToolbarTitleProvider
import com.craftrom.raiderstatus.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : SplashActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun showMainUI(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        val defaultFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)?.childFragmentManager?.fragments?.get(0)
        if (defaultFragment is ToolbarTitleProvider) {
            setToolbarText(defaultFragment.getTitle(), defaultFragment.getSubtitle())
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_char, R.id.nav_guild_list, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    fun setToolbarText(title: String, subtitle: String) {
        supportActionBar?.apply {
            val titleTextView = findViewById<TextView>(R.id.toolbar_title)
            val subtitleTextView = findViewById<TextView>(R.id.toolbar_subtitle)


            titleTextView.text = title
            subtitleTextView.text = subtitle
            titleTextView.maxLines = 1
            subtitleTextView.maxLines = 1

        }
    }
}