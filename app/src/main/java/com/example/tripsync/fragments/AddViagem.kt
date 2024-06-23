package com.example.tripsync.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import com.example.tripsync.utils.NetworkUtils.isNetworkAvailable
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddViagem : Fragment() {

    private lateinit var etTitulo: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etCidade: EditText
    private lateinit var etPais: EditText
    private lateinit var etDataInicio: EditText
    private lateinit var etDataFim: EditText
    private lateinit var etCustos: EditText
    private lateinit var etClassificacao: NumberPicker
    private lateinit var btnCriar: Button
    private var userId: String? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_viagem, container, false)

        // Initialize views
        etTitulo = view.findViewById(R.id.et_titulo)
        etDescricao = view.findViewById(R.id.et_descricao)
        etCidade = view.findViewById(R.id.et_cidade)
        etPais = view.findViewById(R.id.et_pais)
        etDataInicio = view.findViewById(R.id.et_inicio)
        etDataFim = view.findViewById(R.id.et_fim)
        etCustos = view.findViewById(R.id.et_custos)
        etClassificacao = view.findViewById(R.id.et_class)
        btnCriar = view.findViewById(R.id.btn_criar_viagem)

        // Set up NumberPicker
        etClassificacao.minValue = 0
        etClassificacao.maxValue = 10
        etClassificacao.wrapSelectorWheel = false

        // Get user ID and token from shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId == null || token == null) {
            Toast.makeText(activity, getString(R.string.errorUser), Toast.LENGTH_SHORT).show()
            return view
        }

        etDataInicio.setOnClickListener {
            showDatePickerDialog { date -> etDataInicio.setText(date) }
        }

        etDataFim.setOnClickListener {
            showDatePickerDialog { date -> etDataFim.setText(date) }
        }

        btnCriar.setOnClickListener {
            adicionarViagem()
        }

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

    private fun adicionarViagem() {
        val titulo = etTitulo.text.toString()
        val descricao = etDescricao.text.toString()
        val cidade = etCidade.text.toString()
        val pais = etPais.text.toString()
        val dataInicio = etDataInicio.text.toString()
        val dataFim = etDataFim.text.toString()
        val custos = etCustos.text.toString().toFloatOrNull()
        val classificacao = etClassificacao.value

        if (titulo.isEmpty() || descricao.isEmpty() || cidade.isEmpty() || pais.isEmpty() || dataInicio.isEmpty() || dataFim.isEmpty() || custos == null) {
            Toast.makeText(activity, getString(R.string.errorViagemFields), Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dataInicioDate: String
        val dataFimDate: String

        try {
            dataInicioDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataInicio)!!)
            dataFimDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataFim)!!)
        } catch (e: Exception) {
            Toast.makeText(activity, getString(R.string.errorViagemFormat), Toast.LENGTH_SHORT).show()
            return
        }

        val trip = Trip(
            id_viagem = UUID.randomUUID().toString(),
            titulo = titulo,
            descricao = descricao,
            cidade = cidade,
            pais = pais,
            data_inicio = dataInicioDate,
            data_fim = dataFimDate,
            custos = custos,
            classificacao = classificacao,
            id_utilizador = userId!!,
        )

        if (isNetworkAvailable(requireContext())) {
            sendTripToServer(trip)
        } else {
            saveTripRequestLocally(trip)
            Toast.makeText( requireContext(), getString(R.string.localSave), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTripToServer(trip: Trip) {
        ApiClient.apiService.adicionarViagem(trip).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, getString(R.string.okViagem), Toast.LENGTH_SHORT).show()
                    navigateToListViagens()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(activity, getString(R.string.errorViagem), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(activity, getString(R.string.networkerror), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTripRequestLocally(trip: Trip) {
        val sharedPreferences = requireContext().getSharedPreferences("local_viagens_requests", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(trip)
        editor.putString("unsent_viagens_request", json)
        editor.apply()
    }

    private fun navigateToListViagens() {
        val fragment = ListViagens()
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}