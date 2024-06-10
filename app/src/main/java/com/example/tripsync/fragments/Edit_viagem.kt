package com.example.tripsync.fragments

import androidx.fragment.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.User
import com.example.tripsync.api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Edit_viagem: Fragment() {

    private lateinit var et_titulo: EditText
    private lateinit var et_descricao: EditText
    private lateinit var et_cidade: EditText
    private lateinit var et_pais: EditText
    private lateinit var et_inicio: EditText
    private lateinit var et_fim: EditText
    private lateinit var et_custos: EditText
    private lateinit var et_class: EditText
    private lateinit var btn_ficheiros: ImageView
    private lateinit var btn_visitar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnVoltar: ImageButton
    private var userId: String? = null
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
        btn_ficheiros = view.findViewById(R.id.btn_ficheiros)
        btn_visitar = view.findViewById(R.id.btn_visitar)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnVoltar = view.findViewById(R.id.btnVoltar)

        loadViagem()

        btnGuardar.setOnClickListener {
            guardar()
        }

        btnVoltar.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            transaction.replace(R.id.frame_layout, Settings())
            transaction.commit()
        }

        return view
    }

    private fun loadViagem() {
        val sharedPreferences = requireActivity().getSharedPreferences("TripPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId != null && token != null) {
            ApiClient.apiService.getTripDetails(userId!!).enqueue(object : Callback<Trip> {
                override fun onResponse(call: Call<Trip>, response: Response<Trip>) {
                    if (response.isSuccessful) {
                        val trip = response.body()
                        trip?.let {
                            et_titulo.setText(it.titulo)
                            et_descricao.setText(it.descricao)
                            et_cidade.setText(it.cidade)
                            et_pais.setText(it.pais)
                            et_inicio.setText(it.data_inicio)
                            et_fim.setText(it.data_fim)
                            et_custos.setText(it.custos)
                            et_class.setText(it.classificacao)
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
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun guardar() {
        val titulo = et_titulo.text.toString()
        val descricao = et_descricao.text.toString()
        val cidade = et_cidade.text.toString()
        val pais = et_pais.text.toString()
        val data_inicio = et_inicio.text.toString()
        val data_fim = et_fim.text.toString()
        val custos = et_custos.text.toString()
        val clasificacao = et_class.text.toString()
        val foto: ByteArray? = null

        if (userId != null && token != null) {
            val user = User(userId!!, titulo, descricao, cidade, pais, data_inicio, data_fim, custos, clasificacao,  foto)

            ApiClient.apiService.updateUserDetails(userId!!, user).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Trip updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update Trip", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}