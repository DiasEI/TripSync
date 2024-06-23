package com.example.tripsync.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.GetFotosResponse
import com.example.tripsync.api.GetLocalResponse
import com.example.tripsync.api.LocalData
import com.example.tripsync.api.Trip
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class Viagens : Fragment(), OnMapReadyCallback {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescricao: TextView
    private lateinit var tvCidade: TextView
    private lateinit var tvPais: TextView
    private lateinit var tvDataInicio: TextView
    private lateinit var tvDataFim: TextView
    private lateinit var tvCustos: TextView
    private lateinit var tvClassificacao: TextView
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var map: GoogleMap
    private var tripId: String? = null
    private var token: String? = null
    private var tripDetails: Trip? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viagem, container, false)

        tvTitulo = view.findViewById(R.id.tvTitulo)
        tvDescricao = view.findViewById(R.id.tvDescricao)
        tvCidade = view.findViewById(R.id.tvCidade)
        tvPais = view.findViewById(R.id.tvPais)
        tvDataInicio = view.findViewById(R.id.tvDataInicio)
        tvDataFim = view.findViewById(R.id.tvDataFim)
        tvCustos = view.findViewById(R.id.tvCustos)
        tvClassificacao = view.findViewById(R.id.tvClassificacao)
        viewFlipper = view.findViewById(R.id.vfFlipper)
        tripId = arguments?.getString("tripId")

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadViagem()

        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        btnEdit.setOnClickListener {
            val tripId = arguments?.getString("tripId")
            if (tripId != null) {
                val bundle = Bundle().apply {
                    putString("tripId", tripId)
                }
                val editFragment = EditViagem()
                editFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, editFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), getString(R.string.errorUser), Toast.LENGTH_SHORT).show()
            }
        }

        val btnDelete = view.findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        val btnVoltar = view.findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener{
            activity?.supportFragmentManager?.popBackStack()
        }

        val btnSendEmail = view.findViewById<Button>(R.id.btnSendEmail)
        btnSendEmail.setOnClickListener {
            sendTripDetailsByEmail()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        tripId?.let { fetchAndDisplayLocals(it) }
    }

    private fun loadViagem() {
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", null)

        if (tripId != null) {
            ApiClient.apiService.getTripDetails(tripId!!).enqueue(object : Callback<Trip> {
                override fun onResponse(call: Call<Trip>, response: Response<Trip>) {
                    if (response.isSuccessful) {
                        val trip = response.body()
                        trip?.let {
                            tripDetails = it
                            updateTripDetails(it)
                            fetchFotosForViagem(tripId!!)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load trip details", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Trip>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), getString(R.string.errorUser), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTripDetails(trip: Trip) {
        tvTitulo.text = trip.titulo
        tvDescricao.text = trip.descricao
        tvCidade.text = trip.cidade
        tvPais.text = trip.pais

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val dataInicioDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(trip.data_inicio)
            val dataFimDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(trip.data_fim)
            tvDataInicio.text = dataInicioDate?.let { dateFormat.format(it) }
            tvDataFim.text = dataFimDate?.let { dateFormat.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tvClassificacao.text = trip.classificacao.toString()
        tvCustos.text = trip.custos.toString()
    }

    private fun fetchFotosForViagem(viagemId: String) {
        ApiClient.apiService.getFotosByViagem(viagemId).enqueue(object : Callback<GetFotosResponse> {
            override fun onResponse(call: Call<GetFotosResponse>, response: Response<GetFotosResponse>) {
                if (response.isSuccessful) {
                    val fotosResponse = response.body()
                    fotosResponse?.let { fotos ->
                        viewFlipper.removeAllViews()

                        for (fotoData in fotos.fotos) {
                            try {
                                val imageData = fotoData.imageData as String
                                val decodedBytes = Base64.decode(imageData, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                val imageView = ImageView(requireContext())
                                imageView.setImageBitmap(bitmap)
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                                viewFlipper.addView(imageView)
                            } catch (e: IllegalArgumentException) {
                                Log.e("Viagens", "Error decoding Base64 string", e)
                            } catch (e: ClassCastException) {
                                Log.e("Viagens", "Invalid imageData type", e)
                            }
                        }

                        if (fotos.fotos.size > 1) {
                            viewFlipper.isAutoStart = true
                            viewFlipper.flipInterval = 3000
                            viewFlipper.startFlipping()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch photos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetFotosResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAndDisplayLocals(viagemId: String) {
        ApiClient.apiService.getLocalByViagem(viagemId).enqueue(object : Callback<GetLocalResponse> {
            override fun onResponse(call: Call<GetLocalResponse>, response: Response<GetLocalResponse>) {
                if (response.isSuccessful) {
                    val localsResponse = response.body()
                    localsResponse?.let { locals ->
                        displayLocalsOnMap(locals.locais)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch local data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetLocalResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayLocalsOnMap(locals: List<LocalData>) {
        for (local in locals) {
            val latLng = parseLatLng(local.localizacao)
            if (latLng != null) {
                val marker = MarkerOptions()
                    .position(latLng)
                    .title(local.nome)
                    .snippet(local.classificacao.toString())

                map.addMarker(marker)
            }
        }

        // Move camera to the first marker position
        if (locals.isNotEmpty()) {
            val firstLocal = parseLatLng(locals[0].localizacao)
            firstLocal?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
            }
        }
    }

    private fun parseLatLng(location: String): LatLng? {
        val latLngStr = location.split(",")
        return try {
            val lat = latLngStr[0].toDouble()
            val lng = latLngStr[1].toDouble()
            LatLng(lat, lng)
        } catch (e: Exception) {
            Log.e("Viagens", "Error parsing location: $location", e)
            null
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirmDel))
            .setMessage(getString(R.string.confirmDelText))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                tripId?.let { deleteViagem(it) }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteViagem(tripId: String) {
        ApiClient.apiService.deleteViagem(tripId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, "Viagem apagada com sucesso", Toast.LENGTH_SHORT).show()
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(activity, "Erro ao apagar viagem: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(activity, "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendTripDetailsByEmail() {
        tripDetails?.let { trip ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataInicioDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(trip.data_inicio)
            val dataFimDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(trip.data_fim)

            val emailBody = """
                |Trip Details:
                |Title: ${trip.titulo}
                |Description: ${trip.descricao}
                |City: ${trip.cidade}
                |Country: ${trip.pais}
                |Start Date: ${dataInicioDate?.let { dateFormat.format(it) }}
                |End Date: ${dataFimDate?.let { dateFormat.format(it) }}
                |Cost: ${trip.custos}
                |Rating: ${trip.classificacao}
            """.trimMargin()

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_SUBJECT, "Trip Details: ${trip.titulo}")
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email using:"))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No email clients installed.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Trip details are not available", Toast.LENGTH_SHORT).show()
        }
    }
}