package com.example.tripsync

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etName: EditText
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_tele)
        etName = findViewById(R.id.et_nome)

        val btnSelectPhoto: Button = findViewById(R.id.btn_foto)
        btnSelectPhoto.setOnClickListener {
            openGalleryForImage()
        }

        val btnRegister: Button = findViewById(R.id.btn_criar_conta)
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = etPassword.text.toString()
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = etEmail.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val telemovelStr = etPhone.text.toString()
            val telemovel = telemovelStr.toIntOrNull()
            if (telemovel == null) {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nome = etName.text.toString()
            if (nome.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val photoByteArray = selectedPhotoUri?.let { uri ->
                getByteArrayFromUri(uri)
            }

            register(username, email, nome, telemovel, password, photoByteArray)
        }

        val btnVoltar: Button = findViewById(R.id.btn_voltar)
        btnVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                selectedPhotoUri = it
            }
        }
    }

    private fun getByteArrayFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resizedBitmap = resizeBitmap(bitmap, MAX_DIMENSION)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scale = maxDimension.toFloat() / Math.max(width, height)
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
    }

    private fun register(username: String, email: String, nome: String, telemovel: Int, password: String, photoByteArray: ByteArray?) {
        val base64Photo: String? = photoByteArray?.let {
            val base64Image = Base64.encodeToString(it, Base64.DEFAULT)
            "data:image/jpeg;base64,$base64Image"
        }
        val registerRequest = RegisterRequest(username, password, email, telemovel, nome, base64Photo)
        val call = ApiClient.apiService.register(registerRequest)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, getString(R.string.registertrue), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
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

    companion object {
        const val REQUEST_CODE_SELECT_IMAGE = 100
        const val MAX_DIMENSION = 1024
    }
}
