package com.mediakeys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mediakeys.databinding.ActivityMainBinding
import com.mediakeys.service.MediaKeyAccessibilityService

/**
 * Single Activity host. Manages:
 *  - Bottom navigation → fragment swap via NavController
 *  - Listening for service-state broadcasts to keep the UI in sync
 *    even when changed from a notification or boot receiver
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Receives service-state changes from MediaKeyAccessibilityService
    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // The HomeFragment listens to prefs directly via onResume,
            // so just post a dummy update — no-op if fragment isn't visible
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(MediaKeyAccessibilityService.ACTION_SERVICE_STATE_CHANGED)
        registerReceiver(serviceStateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(serviceStateReceiver)
    }
}
