package com.example.tripsync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tripsync.R


class Map : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Now you can initialize the map and do other operations
        // For example:
        // val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        // mapFragment?.getMapAsync { googleMap ->
        //     // You can customize the map here
        // }
    }

}