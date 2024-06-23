package com.example.tripsync.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
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
import java.security.MessageDigest

class Home : Fragment() {
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1

    private val imageViewIds = listOf(
        R.id.rec1, R.id.rec2, R.id.rec3, R.id.rec4,
        R.id.rec5, R.id.rec6, R.id.rec7, R.id.rec8
    )
    private val textViewIds = listOf(
        R.id.rec1_text, R.id.rec2_text, R.id.rec3_text, R.id.rec4_text,
        R.id.rec5_text, R.id.rec6_text, R.id.rec7_text, R.id.rec8_text
    )

    private lateinit var imageViews: List<ImageView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize ImageViews
        imageViews = imageViewIds.map { id -> view.findViewById<ImageView>(id) }
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
                for ((index, placeLikelihood) in response.placeLikelihoods.withIndex()) {
                    if (index >= imageViewIds.size) break

                    val place = placeLikelihood.place
                    // Load photos into the respective ImageViews and set place names to TextViews
                    val imageView = view?.findViewById<ImageView>(imageViewIds[index])
                    val textView = view?.findViewById<TextView>(textViewIds[index])

                    imageView?.let { loadPlacePhotos(place, it) }
                    textView?.text = place.name
                }
            }.addOnFailureListener { exception: Exception ->
                Snackbar.make(requireView(), "Failed to get current place", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun loadPlacePhotos(place: Place, imageView: ImageView) {
        val photoMetadataList = place.photoMetadatas

        photoMetadataList?.let {
            if (it.isNotEmpty()) {
                val photoMetadata = it.first()

                photoMetadata?.let { metadata ->
                    val photoRequest = FetchPhotoRequest.builder(metadata)
                        .setMaxWidth(150)
                        .setMaxHeight(150)
                        .build()

                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
                        val bitmap = fetchPhotoResponse.bitmap

                        Glide.with(this)
                            .asBitmap()
                            .load(bitmap)
                            .apply(RequestOptions.bitmapTransform(CircleCropTransformation()))
                            .into(imageView)
                    }.addOnFailureListener { exception ->
                        Snackbar.make(requireView(), "Failed to fetch photo for place", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageViews.forEach { imageView ->
            Glide.with(imageView)
                .clear(imageView)
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
        val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), "Google Maps app is not installed", Toast.LENGTH_SHORT).show()
        }
    }

}

class CircleCropTransformation : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("circle crop transformation".toByteArray())
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return TransformationUtils.circleCrop(pool, toTransform, outWidth, outHeight)
    }
}
