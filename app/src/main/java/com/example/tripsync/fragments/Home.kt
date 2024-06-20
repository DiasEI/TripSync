package com.example.tripsync.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tripsync.LoginActivity
import com.example.tripsync.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class Home : Fragment() {
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1

    private val imageViewIds = listOf(
        R.id.rec1, R.id.rec2, R.id.rec3, R.id.rec4,
        R.id.rec5, R.id.rec6, R.id.rec7, R.id.rec8
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Places SDK
        Places.initialize(requireContext(), getString(R.string.MAPS_API_KEY))
        placesClient = Places.createClient(requireContext())

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getCurrentPlace()

        val logoutButton: ImageButton = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        val search: EditText = view.findViewById(R.id.search)
        val btnSearch: MaterialButton = view.findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener {
            val query = search.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        return view

    }

    private fun getCurrentPlace() {
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS
        )

        val request = FindCurrentPlaceRequest.builder(placeFields).build()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            placesClient.findCurrentPlace(request).addOnSuccessListener { response: FindCurrentPlaceResponse ->
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place

                    // Display place information
                    Log.i("Location", "Place '${place.name}' has likelihood: ${placeLikelihood.likelihood}")

                    loadPlacePhotos(place)
                }
            }.addOnFailureListener { exception: Exception ->
                Log.e("Location", "Failed to get current place: ${exception.message}")
                Snackbar.make(requireView(), "Failed to get current place", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun loadPlacePhotos(place: Place) {
        val photoMetadataList = place.photoMetadatas

        photoMetadataList?.let {
            for (i in it.indices) {
                if (i >= imageViewIds.size) {
                    break
                }

                val imageView = view?.findViewById<ImageView>(imageViewIds[i])
                val photoMetadata = photoMetadataList[i]

                photoMetadata?.let { metadata ->
                    val photoRequest = FetchPhotoRequest.builder(metadata)
                        .setMaxWidth(150)
                        .setMaxHeight(150)
                        .build()

                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
                        val bitmap = fetchPhotoResponse.bitmap
                        imageView?.setImageBitmap(bitmap)
                    }.addOnFailureListener { exception ->
                        Snackbar.make(requireView(), "Failed to fetch photo for place", Snackbar.LENGTH_SHORT).show()
                        Log.e("PhotoFetch", "Error fetching photo: $exception")
                    }
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Snackbar.make(
                requireView(),
                "Location permission is required to show nearby places.",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("OK") {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }.show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentPlace()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        // Implement your search logic here
        // For example, you could filter a list of items, make a network request, etc.
        // Here's a simple example that prints the query to the log
        println("Search query: $query")
        // You can replace the println with your actual search logic
    }

}
