package com.degoogled.androidauto

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.degoogled.androidauto.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        
        // Default to navigation view
        binding.bottomNavigation.selectedItemId = R.id.navigation
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation -> {
                // Load navigation view
                return true
            }
            R.id.media -> {
                // Load media view
                return true
            }
            R.id.phone -> {
                // Load phone view
                return true
            }
            R.id.messaging -> {
                // Load messaging view
                return true
            }
            R.id.settings -> {
                // Load settings view
                return true
            }
        }
        return false
    }
}