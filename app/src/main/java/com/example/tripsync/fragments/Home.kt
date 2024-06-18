package com.example.tripsync.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.tripsync.LoginActivity
import com.example.tripsync.R

class Home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val logoutButton: ImageButton = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            // Start LoginActivity
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }


        val search: EditText = view.findViewById(R.id.search)
        val btnSearch: ImageButton = view.findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener {
            val query = search.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }


        return view


    }

    private fun performSearch(query: String) {
        // Implement your search logic here
        // For example, you could filter a list of items, make a network request, etc.
        // Here's a simple example that prints the query to the log
        println("Search query: $query")
        // You can replace the println with your actual search logic
    }



}
