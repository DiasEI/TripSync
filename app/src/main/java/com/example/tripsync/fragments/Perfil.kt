package com.example.tripsync.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.tripsync.R

class Perfil : Fragment() {

    private lateinit var etNome: EditText
    private lateinit var etTel: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnFoto: Button
    private lateinit var btnGuarda: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.perfil, container, false)

        etNome = view.findViewById(R.id.et_nome)
        etTel = view.findViewById(R.id.et_tel)
        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.et_password)
        btnFoto = view.findViewById(R.id.btnFoto)
        btnGuarda = view.findViewById(R.id.btnGuarda)

        loadProfile()

        btnGuarda.setOnClickListener {
            guardar()
        }

        return view
    }

    private fun loadProfile() {
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val nome = sharedPreferences.getString("nome", "")
        val tel = sharedPreferences.getString("tel", "")
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")

        etNome.setText(nome)
        etTel.setText(tel)
        etEmail.setText(email)
        etPassword.setText(password)
    }
    private fun guardar() {
        val nome = etNome.text.toString()
        val tel = etTel.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("nome", nome)
        editor.putString("tel", tel)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }
}
