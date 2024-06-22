package com.example.tripsync.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Viagens : Fragment() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescricao: TextView
    private lateinit var tvCidade: TextView
    private lateinit var tvPais: TextView
    private lateinit var tvDataInicio: TextView
    private lateinit var tvDataFim: TextView
    private lateinit var tvCustos: TextView
    private lateinit var tvClassificacao: TextView
    private var tripId: String? = null
    private var token: String? = null

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

        // Get tripId from arguments
        tripId = arguments?.getString("tripId")

        loadViagem() // Load the trip details

        return view
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
                            tvTitulo.text = it.titulo
                            tvDescricao.text = it.descricao
                            tvCidade.text = it.cidade
                            tvPais.text = it.pais

                            // Convert the date strings to Date objects
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            try {
                                val dataInicioDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.data_inicio)
                                val dataFimDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.data_fim)
                                tvDataInicio.text = dataInicioDate?.let { date -> dateFormat.format(date) }
                                tvDataFim.text = dataFimDate?.let { date -> dateFormat.format(date) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            tvClassificacao.text = it.classificacao.toString()
                            tvCustos.text = it.custos.toString()
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
            Toast.makeText(requireContext(), "User not logged in or tripId missing", Toast.LENGTH_SHORT).show()
        }
    }
}