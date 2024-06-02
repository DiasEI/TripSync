package com.example.tripsync

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.RegisterRequest
import com.example.tripsync.api.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_tele)
        etName = findViewById(R.id.et_nome)

        val btnRegister: Button = findViewById(R.id.btn_criar_conta)
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()
            val name = etName.text.toString()
            register(username, password, email, phone, name)
        }
    }

    private fun register(username: String, password: String, email: String, phone: String, name: String) {
        val registerRequest = RegisterRequest(username, password, email, phone, name, null)
        val call = ApiClient.apiService.register(registerRequest)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        Toast.makeText(this@RegisterActivity, getString(R.string.registertrue), Toast.LENGTH_SHORT).show()
                        // Navigate to MainMenuActivity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, registerResponse?.message ?: getString(R.string.registerfalse), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, getString(R.string.registerfalse), Toast.LENGTH_SHORT).show()
                }
            }

            @SuppressLint("StringFormatInvalid")
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, getString(R.string.networkerror, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

}