package com.example.tripsync

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.LoginRequest
import com.example.tripsync.api.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity: AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.et_login_username)
        etPassword = findViewById(R.id.et_login_password)

        val btnLogin: Button = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            login(username, password)
        }

        val btnConta: Button = findViewById(R.id.btnConta)
        btnConta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        val call = ApiClient.apiService.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    // Lida com o login bem-sucedido
                    Toast.makeText(this@LoginActivity, getString(R.string.logintrue), Toast.LENGTH_SHORT).show()
                    // Redireciona para o MainMenu
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Lidar com login mal-sucedido
                    Toast.makeText(this@LoginActivity, getString(R.string.loginfalse), Toast.LENGTH_SHORT).show()
                }
            }

            @SuppressLint("StringFormatInvalid")
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Lida com problemas de conexão
                Toast.makeText(this@LoginActivity, getString(R.string.networkerror, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

}