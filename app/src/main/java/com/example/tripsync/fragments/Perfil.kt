package com.example.tripsync.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Perfil : Fragment() {

    private lateinit var etNome: EditText
    private lateinit var etTel: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etUsername: EditText
    private lateinit var profilePhoto: ImageView
    private lateinit var btnGuarda: Button
    private lateinit var btnVoltar: ImageButton
    private var userId: String? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        etNome = view.findViewById(R.id.et_nome)
        etTel = view.findViewById(R.id.et_tel)
        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.et_password)
        etUsername = view.findViewById(R.id.et_username)
        profilePhoto = view.findViewById(R.id.profile_photo)
        btnGuarda = view.findViewById(R.id.btnGuarda)
        btnVoltar = view.findViewById(R.id.btn_voltar)

        loadProfile()

        btnGuarda.setOnClickListener {
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

    private fun loadProfile() {
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId != null && token != null) {
            ApiClient.apiService.getUserDetails(userId!!).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        user?.let {
                            etNome.setText(it.nome)
                            etTel.setText(it.telemovel.toString())
                            etEmail.setText(it.email)
                            etUsername.setText(it.username)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
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

    private fun guardar() {
        val nome = etNome.text.toString()
        val tel = etTel.text.toString().toInt()
        val email = etEmail.text.toString()
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()
        val foto: ByteArray? = null

        if (userId != null && token != null) {
            val user = User(userId!!, nome, username, tel, email, password, foto)

            ApiClient.apiService.updateUserDetails(userId!!, user).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
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
