package com.example.tripsync.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.api.AddLocalRequest
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.LocalData
import com.example.tripsync.utils.NetworkUtils.isNetworkAvailable
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Arrays

class AddLocal : Fragment() {

    private lateinit var etNome: EditText
    private lateinit var etLocalizacao: Button
    private lateinit var etTipo: EditText
    private lateinit var etClassificacao: NumberPicker
    private lateinit var btnGuarda: Button
    private var tripId: String? = null

    private lateinit var placesClient: PlacesClient
    private var selectedCoordinates: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_local, container, false)

        etNome = view.findViewById(R.id.et_nome)
        etLocalizacao = view.findViewById(R.id.btnAdd)
        etTipo = view.findViewById(R.id.et_tipo)
        etClassificacao = view.findViewById(R.id.et_class)
        tripId = arguments?.getString("tripId")

        etClassificacao.minValue = 0
        etClassificacao.maxValue = 10
        etClassificacao.wrapSelectorWheel = false

        val btnVoltar = view.findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        btnGuarda = view.findViewById(R.id.btnGuarda)
        btnGuarda.setOnClickListener {
            addLocal()
        }

        Places.initialize(requireContext(), getString(R.string.MAPS_API_KEY))
        placesClient = Places.createClient(requireContext())

        etLocalizacao.setOnClickListener {
            startPlacePicker()
        }

        return view
    }

    private fun startPlacePicker() {
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        val vianaDoCastelo = LatLng(41.6935, -8.8326)
        val biasRect = RectangularBounds.newInstance(
            LatLng(vianaDoCastelo.latitude - 0.1, vianaDoCastelo.longitude - 0.1),
            LatLng(vianaDoCastelo.latitude + 0.1, vianaDoCastelo.longitude + 0.1)
        )

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setLocationBias(biasRect)
            .build(requireContext())

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    etLocalizacao.text = place.name
                    selectedCoordinates = place.latLng
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(requireContext(), getString(R.string.cancelPlace), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addLocal() {
        val nome = etNome.text.toString().trim()
        val localizacao = selectedCoordinates?.let { "${it.latitude},${it.longitude}" } ?: ""
        val tipo = etTipo.text.toString().trim()
        val classificacao = etClassificacao.value

        val localData = LocalData(null, nome, localizacao, tipo, classificacao)
        val localRequest = AddLocalRequest(tripId ?: "", listOf(localData))

        if (isNetworkAvailable(requireContext())) {
            sendAddLocalRequest(localRequest)
        } else {
            saveLocalRequest(localRequest)
            Toast.makeText(requireContext(), getString(R.string.localSave), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendAddLocalRequest(request: AddLocalRequest) {
        val call = ApiClient.apiService.addLocal(request)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.okLocal), Toast.LENGTH_SHORT).show()
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.errorLocal), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), getString(R.string.networkerror), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveLocalRequest(request: AddLocalRequest) {
        val sharedPreferences = requireContext().getSharedPreferences("local_requests", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(request)
        editor.putString("unsent_request", json)
        editor.apply()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1001
    }
}
