package com.example.tripsync.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class EditViagem : Fragment() {

    private lateinit var et_titulo: EditText
    private lateinit var et_descricao: EditText
    private lateinit var et_cidade: EditText
    private lateinit var et_pais: EditText
    private lateinit var et_inicio: EditText
    private lateinit var et_fim: EditText
    private lateinit var et_custos: EditText
    private lateinit var et_class: NumberPicker
    private var selectedImageUri: Uri? = null
    private lateinit var btn_ficheiros: ImageView
    private lateinit var btn_visitar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnVoltar: ImageButton
    private var userId: String? = null
    private var tripId: String? = null
    private var token: String? = null
    private val PICK_IMAGE_REQUEST = 1

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
        btn_ficheiros = view.findViewById(R.id.btn_ficheiros)
        btn_visitar = view.findViewById(R.id.btn_visitar)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnVoltar = view.findViewById(R.id.btnVoltar)


        // Set up NumberPicker
        et_class.minValue = 0
        et_class.maxValue = 10

        // Get tripId from arguments
        tripId = arguments?.getString("tripId")


        loadViagem()

        btn_ficheiros.setOnClickListener {
            openGallery()
        }

        btnGuardar.setOnClickListener {
            guardar()
        }

        btnVoltar.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .into(btn_ficheiros)
        }
    }

    private fun loadViagem() {
        val sharedPreferences = requireActivity().getSharedPreferences("TripPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId != null && token != null && tripId != null) {
            ApiClient.apiService.getTripDetails(tripId!!).enqueue(object : Callback<Trip> {
                override fun onResponse(call: Call<Trip>, response: Response<Trip>) {
                    if (response.isSuccessful) {
                        val trip = response.body()
                        trip?.let {
                            et_titulo.setText(it.titulo)
                            et_descricao.setText(it.descricao)
                            et_cidade.setText(it.cidade)
                            et_pais.setText(it.pais)

                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            et_inicio.setText(dateFormat.format(it.data_inicio))
                            et_fim.setText(dateFormat.format(it.data_fim))
                            et_class.setValue(it.classificacao)
                            et_custos.setText(it.custos.toString())



                            it.foto?.let { foto ->
                                when (foto) {
                                    is ByteArray -> {
                                        Glide.with(this@EditViagem)
                                            .load(foto)
                                            .into(btn_ficheiros)
                                    }
                                    is Map<*, *> -> {
                                        val fotoData =
                                            (foto["data"] as List<Number>).map { it.toByte() }
                                                .toByteArray()
                                        Glide.with(this@EditViagem)
                                            .load(fotoData)
                                            .into(btn_ficheiros)
                                    }
                                    else -> {
                                    }
                                }
                            }
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
        val custos = et_custos.text.toString().toFloat()
        val classificacao = et_class.value
        var fotoData: String? = null


        if (selectedImageUri != null) {
            try {
                val inputStream: InputStream? = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                val photoBytes = inputStream?.readBytes()
                fotoData = Base64.encodeToString(photoBytes, Base64.DEFAULT)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (titulo.isEmpty() || descricao.isEmpty() || cidade.isEmpty() || pais.isEmpty() || data_inicio.isEmpty() || data_fim.isEmpty() || custos == null || classificacao == null) {
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
            return
        }

        if (userId != null && token != null && tripId != null) {
            val trip = Trip(
                id_viagem = tripId!!,
                titulo = titulo,
                descricao = descricao,
                cidade = cidade,
                pais = pais,
                data_inicio = dataInicioDate,
                data_fim = dataFimDate,
                custos = custos,
                classificacao = classificacao,
                id_utilizador = userId!!,
                foto = fotoData
            )

            ApiClient.apiService.updateTripDetails(tripId!!, trip).enqueue(object : Callback<Trip> {
                override fun onResponse(call: Call<Trip>, response: Response<Trip>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Viagem atualizada com sucesso", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Erro ao atualizar viagem", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Trip>, t: Throwable) {
                    Toast.makeText(requireContext(), "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in or tripId missing", Toast.LENGTH_SHORT).show()
        }
    }
}
