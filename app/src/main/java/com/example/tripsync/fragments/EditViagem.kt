package com.example.tripsync.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditViagem : Fragment() {

    private lateinit var et_titulo: EditText
    private lateinit var et_descricao: EditText
    private lateinit var et_cidade: EditText
    private lateinit var et_pais: EditText
    private lateinit var et_inicio: EditText
    private lateinit var et_fim: EditText
    private lateinit var et_custos: EditText
    private lateinit var et_class: NumberPicker
    private lateinit var btnGuardar: Button
    private var userId: String? = null
    private var tripId: String? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_viagem_fragment, container, false)

        et_titulo = view.findViewById(R.id.et_titulo)
        et_descricao = view.findViewById(R.id.et_descricao)
        et_cidade = view.findViewById(R.id.et_cidade)
        et_pais = view.findViewById(R.id.et_pais)
        et_inicio = view.findViewById(R.id.et_inicio)
        et_fim = view.findViewById(R.id.et_fim)
        et_custos = view.findViewById(R.id.et_custos)
        et_class = view.findViewById(R.id.et_class)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        et_class.minValue = 0
        et_class.maxValue = 10

        tripId = arguments?.getString("tripId")

        et_inicio.setOnClickListener {
            showDatePickerDialog { date -> et_inicio.setText(date) }
        }

        et_fim.setOnClickListener {
            showDatePickerDialog { date -> et_fim.setText(date) }
        }

        btnGuardar.setOnClickListener {
            guardar()
        }

        val btnVoltar = view.findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener{
            activity?.supportFragmentManager?.popBackStack()
        }

        loadViagem()

        return view
    }

    private fun showDatePickerDialog(onDateSet: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            onDateSet(dateFormat.format(selectedDate.time))
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadViagem() {
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (tripId != null) {
            ApiClient.apiService.getTripDetails(tripId!!).enqueue(object : Callback<Trip> {
                override fun onResponse(call: Call<Trip>, response: Response<Trip>) {
                    if (response.isSuccessful) {
                        val trip = response.body()
                        trip?.let {
                            et_titulo.setText(it.titulo)
                            et_descricao.setText(it.descricao)
                            et_cidade.setText(it.cidade)
                            et_pais.setText(it.pais)

                            // Convert the date strings to Date objects
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            try {
                                val dataInicioDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.data_inicio)
                                val dataFimDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.data_fim)
                                et_inicio.setText(dataInicioDate?.let { date -> dateFormat.format(date) })
                                et_fim.setText(dataFimDate?.let { date -> dateFormat.format(date) })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            et_class.value = it.classificacao
                            et_custos.setText(it.custos.toString())
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


    private fun guardar() {
        val titulo = et_titulo.text.toString()
        val descricao = et_descricao.text.toString()
        val cidade = et_cidade.text.toString()
        val pais = et_pais.text.toString()
        val data_inicio = et_inicio.text.toString()
        val data_fim = et_fim.text.toString()
        val custos = et_custos.text.toString().toFloatOrNull()
        val classificacao = et_class.value

        if (titulo.isEmpty() || descricao.isEmpty() || cidade.isEmpty() || pais.isEmpty() || data_inicio.isEmpty() || data_fim.isEmpty() || custos == null) {
            Toast.makeText(activity, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dataInicioDate: String
        val dataFimDate: String

        try {
            dataInicioDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(data_inicio)!!)
            dataFimDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(data_fim)!!)
        } catch (e: Exception) {
            Toast.makeText(activity, "Formato de data inválido", Toast.LENGTH_SHORT).show()
            Log.d("Guardar", "Date parsing error: ${e.message}")
            return
        }

        if (userId != null && token != null && tripId != null) {
            val trip = Trip(
                id_viagem = tripId!!,
                titulo = titulo,
                descricao = descricao,
                cidade = cidade,
                custos = custos,
                data_inicio = dataInicioDate,
                data_fim = dataFimDate,
                classificacao = classificacao,
                pais = pais,
                id_utilizador = userId!!,
            )

            // Log the request data
            Log.d("Guardar", "Request data: $trip")

            ApiClient.apiService.updateTripDetails(tripId!!, trip).enqueue(object : Callback<Trip> {
                override fun onResponse(call: Call<Trip>, response: Response<Trip>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Viagem atualizada com sucesso", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Erro ao atualizar viagem", Toast.LENGTH_SHORT).show()
                        Log.d("Guardar", "API response error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Trip>, t: Throwable) {
                    Toast.makeText(requireContext(), "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d("Guardar", "API call failure: ${t.message}")
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in or tripId missing", Toast.LENGTH_SHORT).show()
            Log.d("Guardar", "Authentication error: User not logged in or tripId is missing.")
        }
    }
}
