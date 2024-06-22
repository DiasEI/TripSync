package com.example.tripsync

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tripsync.fragments.AddViagem
import com.example.tripsync.fragments.Adicionar
import com.example.tripsync.fragments.Home
import com.example.tripsync.fragments.ListViagens
import com.example.tripsync.fragments.Perfil
import com.example.tripsync.fragments.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation_view)

        // Load the default fragment
        if (savedInstanceState == null) {
            loadFragment(Home())
        }

        // Set up the item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.home -> {
                    loadFragment(Home())
                    true
                }
                R.id.add -> {
                    loadFragment(AddViagem())
                    true
                }
                R.id.settings -> {
                    loadFragment(Settings())
                    true
                }
                R.id.perf -> {
                    loadFragment(ListViagens())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // Get the currently visible fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        // Check if the new fragment is different from the current one
        if (currentFragment == null || currentFragment.javaClass != fragment.javaClass) {
            // Load the new fragment into the frame layout
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit()
        }
    }
}
