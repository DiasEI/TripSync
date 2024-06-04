package com.example.tripsync

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class perfilActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etTel: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnFoto: Button
    private lateinit var btnGuarda: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        etNome = findViewById(R.id.et_nome)
        etTel = findViewById(R.id.et_tel)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnFoto = findViewById(R.id.btnFoto)
        btnGuarda = findViewById(R.id.btnGuarda)

        loadProfile()

        btnGuarda.setOnClickListener {
            guardar()
        }
    }

    private fun loadProfile() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
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

        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("nome", nome)
        editor.putString("tel", tel)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

}