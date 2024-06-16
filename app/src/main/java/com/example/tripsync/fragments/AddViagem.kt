package com.example.tripsync.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView

class AddViagem : Fragment() {

    private lateinit var etTitulo: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etCidade: EditText
    private lateinit var etPais: EditText
    private lateinit var etDataInicio: EditText
    private lateinit var etDataFim: EditText
    private lateinit var etCustos: EditText
    private lateinit var etClassificacao: EditText
    private lateinit var btnCriar: Button
    private lateinit var btnSelecionarFoto: Button
    private lateinit var previewImagem: ImageView

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
        previewImagem = view.findViewById(R.id.preview_imagem)

        // Get user ID and token from shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId == null || token == null) {
            Toast.makeText(activity, "Erro: Usuário não autenticado", Toast.LENGTH_SHORT).show()
            Log.e("AddViagem", "Usuário não autenticado")
            return view
        }

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
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            previewImagem.visibility = View.VISIBLE
            previewImagem.setImageURI(selectedImageUri)
            Toast.makeText(activity, "Imagem selecionada com sucesso", Toast.LENGTH_SHORT).show()
            Log.d("AddViagem", "Imagem selecionada: $selectedImageUri")
        }
    }

    private fun adicionarViagem() {
        val titulo = etTitulo.text.toString()
        val descricao = etDescricao.text.toString()
        val cidade = etCidade.text.toString()
        val pais = etPais.text.toString()
        val dataInicio = etDataInicio.text.toString()
        val dataFim = etDataFim.text.toString()
        val custos = etCustos.text.toString().toFloatOrNull()
        val classificacao = etClassificacao.text.toString().toIntOrNull()

        if (titulo.isEmpty() || descricao.isEmpty() || cidade.isEmpty() || pais.isEmpty() || dataInicio.isEmpty() || dataFim.isEmpty() || custos == null || classificacao == null) {
            Toast.makeText(activity, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            Log.e("AddViagem", "Campos não preenchidos corretamente")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dataInicioDate: String
        val dataFimDate: String

        try {
            dataInicioDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataInicio))
            dataFimDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataFim))
        } catch (e: Exception) {
            Toast.makeText(activity, "Formato de data inválido", Toast.LENGTH_SHORT).show()
            Log.e("AddViagem", "Formato de data inválido", e)
            return
        }

        val trip = Trip(titulo, descricao, cidade, pais, dataInicioDate, dataFimDate, custos, classificacao, userId!!, selectedImageUri.toString())

        ApiClient.apiService.adicionarViagem(trip).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, "Viagem adicionada com sucesso", Toast.LENGTH_SHORT).show()
                    Log.d("AddViagem", "Viagem adicionada com sucesso")
                    // Optionally, navigate back or clear fields
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

    companion object {
        private const val REQUEST_IMAGE = 100
    }
}