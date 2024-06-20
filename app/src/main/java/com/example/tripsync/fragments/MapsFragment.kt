package com.example.tripsync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private lateinit var map: GoogleMap
    private var waypointMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        // Example marker added for Sydney, Australia
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Optionally, enable user interaction with the map
        googleMap.setOnMapClickListener { latLng ->
            addWaypointMarker(latLng)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        return root
    }

    private fun addWaypointMarker(latLng: LatLng) {
        // Remove previous waypoint marker if exists
        waypointMarker?.remove()
        // Add new waypoint marker
        waypointMarker = map.addMarker(MarkerOptions().position(latLng).title("Waypoint"))
        // Optionally, animate camera to the new waypoint
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }
}