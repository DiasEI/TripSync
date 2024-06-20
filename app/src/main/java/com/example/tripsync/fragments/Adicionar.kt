package com.example.tripsync.fragments

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream


class Adicionar: Fragment() {
    private lateinit var et_nome: EditText
    private lateinit var et_class: EditText
    private lateinit var btnAdd: Button
    private lateinit var et_tipo: EditText

    private lateinit var btnGuarda: Button
    private lateinit var btnVoltar: Button
    private var userId: String? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.adicionar_fragment, container, false)

        et_nome = view.findViewById(R.id.et_nome)
        et_class = view.findViewById(R.id.et_class)
        et_tipo = view.findViewById(R.id.et_tipo)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnGuarda = view.findViewById(R.id.btnGuarda)
        btnVoltar = view.findViewById(R.id.btnVoltar)



        btnAdd.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            transaction.replace(R.id.frame_layout, MapsFragment())
            transaction.commit()
        }

        btnVoltar.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }



        return view
    }



}

