package com.example.tripsync.fragments

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.example.tripsync.LoginActivity
import com.example.tripsync.R
import com.example.tripsync.workers.SendFotoRequestsWorker
import com.example.tripsync.workers.SendLocalRequestsWorker
import com.example.tripsync.workers.SendViagemRequestsWorker
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class Home : Fragment() {
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
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

        imageViews = imageViewIds.map { id -> view.findViewById<ImageView>(id) }
        Places.initialize(requireContext(), getString(R.string.MAPS_API_KEY))
        placesClient = Places.createClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize Autocomplete fragment
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment)
                as AutocompleteSupportFragment

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up the Autocomplete search
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Handle selected place
                val placeName = place.name
                val placeLatLng = place.latLng

                // Launch map fragment with selected place
                val bundle = Bundle().apply {
                    putParcelable("placeLatLng", placeLatLng)
                    putString("placeName", placeName)
                }
                val mapsFragment = MapsFragment()
                mapsFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, mapsFragment)
                    .addToBackStack(null)
                    .commit()
            }

            override fun onError(status: Status) {
                // Handle error
                Log.e(TAG, "Error: ${status.statusMessage}")
                Snackbar.make(requireView(), "Error: ${status.statusMessage}", Snackbar.LENGTH_SHORT).show()
            }
        })

        scheduleSendRequestsWork(requireContext())
        getCurrentPlace()

        val logoutButton: ImageButton = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
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

    fun scheduleSendRequestsWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val sendLocalRequestsRequest = PeriodicWorkRequestBuilder<SendLocalRequestsWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        val sendFotoRequestWorker = PeriodicWorkRequestBuilder<SendFotoRequestsWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        val sendViagemRequestsRequest = PeriodicWorkRequestBuilder<SendViagemRequestsWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sendLocalRequestsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            sendLocalRequestsRequest
        )

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sendFotoRequestWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            sendFotoRequestWorker
        )

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sendViagemRequestsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            sendViagemRequestsRequest
        )

        Log.d("OfflineData", "Scheduled Offline Request")
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
