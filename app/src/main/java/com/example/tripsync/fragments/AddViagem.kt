package com.example.tripsync.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.RegisterActivity.Companion.MAX_DIMENSION
import com.example.tripsync.RegisterActivity.Companion.REQUEST_CODE_SELECT_IMAGE
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var btnSelecionarFoto: Button
    private lateinit var btnVisitar: Button

    private var userId: String? = null
    private var token: String? = null

    private var selectedImageUri: Uri? = null

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
        btnSelecionarFoto = view.findViewById(R.id.btn_selecionar_foto)
        btnVisitar = view.findViewById(R.id.btn_visitar)

        // Set up NumberPicker
        etClassificacao.minValue = 0
        etClassificacao.maxValue = 10
        etClassificacao.wrapSelectorWheel = false

        // Get user ID and token from shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId == null || token == null) {
            Toast.makeText(activity, "Erro: Usuário não autenticado", Toast.LENGTH_SHORT).show()
            Log.e("AddViagem", "Usuário não autenticado")
            return view
        }

        // Adicione logs para verificar o userId e o token
        Log.d("AddViagem", "User ID: $userId, Token: $token")

        // Set onClickListener for date fields
        etDataInicio.setOnClickListener {
            showDatePickerDialog { date -> etDataInicio.setText(date) }
        }

        etDataFim.setOnClickListener {
            showDatePickerDialog { date -> etDataFim.setText(date) }
        }

        // Set onClickListener for Selecionar Foto button
        btnSelecionarFoto.setOnClickListener {
            selecionarFoto()
        }

        // Set onClickListener for the Criar Viagem button
        btnCriar.setOnClickListener {
            adicionarViagem()
        }

        // Set onClickListener for the Visitar button
        btnVisitar.setOnClickListener {
            // Intent to open Google Maps
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(etCidade.text.toString() + ", " + etPais.text.toString()))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
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

    private fun selecionarFoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                selectedImageUri = it
                Log.d("AddViagem", "Imagem selecionada: $selectedImageUri")
            }
            Toast.makeText(activity, "Imagem selecionada com sucesso", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("AddViagem", "Seleção de imagem cancelada ou falhou. Result Code: $resultCode")
        }
    }

    private fun getByteArrayFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
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
            Toast.makeText(activity, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            Log.e("AddViagem", "Campos não preenchidos corretamente")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dataInicioDate: String
        val dataFimDate: String

        try {
            dataInicioDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataInicio)!!)
            dataFimDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataFim)!!)
        } catch (e: Exception) {
            Toast.makeText(activity, "Formato de data inválido", Toast.LENGTH_SHORT).show()
            Log.e("AddViagem", "Formato de data inválido", e)
            return
        }

        val photoByteArray = selectedImageUri?.let { uri ->
            getByteArrayFromUri(uri)
        }

        val base64Photo: String? = photoByteArray?.let {
            val base64Image = Base64.encodeToString(it, Base64.DEFAULT)
            "data:image/jpeg;base64,$base64Image"
        }

        val trip = Trip(
            titulo = titulo,
            descricao = descricao,
            cidade = cidade,
            pais = pais,
            data_inicio = dataInicioDate,
            data_fim = dataFimDate,
            custos = custos,
            classificacao = classificacao,
            id_utilizador = userId!!,
            foto = base64Photo
        )

        // Adicione logs para verificar os dados da viagem antes de enviar a solicitação
        Log.d("AddViagem", "Trip data: $trip")

        ApiClient.apiService.adicionarViagem(trip).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, "Viagem adicionada com sucesso", Toast.LENGTH_SHORT).show()
                    Log.d("AddViagem", "Viagem adicionada com sucesso")
                    // Clear fields on success
                    clearFields()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(activity, "Erro ao adicionar viagem: $errorBody", Toast.LENGTH_SHORT).show()
                    Log.e("AddViagem", "Erro ao adicionar viagem: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(activity, "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AddViagem", "Erro de rede", t)
            }
        })
    }

    private fun clearFields() {
        etTitulo.text.clear()
        etDescricao.text.clear()
        etCidade.text.clear()
        etPais.text.clear()
        etDataInicio.text.clear()
        etDataFim.text.clear()
        etCustos.text.clear()
        etClassificacao.value = 0
        selectedImageUri = null
    }
}
