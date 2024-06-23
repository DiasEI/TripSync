package com.example.tripsync.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.RegisterActivity.Companion.MAX_DIMENSION
import com.example.tripsync.RegisterActivity.Companion.REQUEST_CODE_SELECT_IMAGE
import com.example.tripsync.adapters.FotosAdapter
import com.example.tripsync.api.AddFotosRequest
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.FotoData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddFoto : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSelecionarFoto: Button
    private lateinit var btnAdicionarFoto: Button
    private lateinit var adapter: FotosAdapter
    private val selectedFotoUris: MutableList<Uri> = mutableListOf()
    private var tripId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_foto, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        btnSelecionarFoto = view.findViewById(R.id.btnSelecionarFoto)
        btnAdicionarFoto = view.findViewById(R.id.btnAdicionarFoto)

        adapter = FotosAdapter(selectedFotoUris, requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        tripId = arguments?.getString("tripId")

        btnSelecionarFoto.setOnClickListener {
            openGalleryForImage()
        }

        btnAdicionarFoto.setOnClickListener {
            uploadSelectedFotos()
        }

        val btnVoltar = view.findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener{
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
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
                selectedFotoUris.add(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun uploadSelectedFotos() {
        val fotoDataList: MutableList<FotoData> = mutableListOf()

        for (uri in selectedFotoUris) {
            val fotoByteArray = getByteArrayFromUri(uri)
            if (fotoByteArray != null) {
                val base64Foto = encodeImageToBase64(fotoByteArray)
                if (base64Foto != null) {
                    val fotoData = FotoData(null, base64Foto)
                    fotoDataList.add(fotoData)
                } else {
                    Toast.makeText(requireContext(), "Failed to encode foto", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val addFotosRequest = tripId?.let { tripId ->
            AddFotosRequest(tripId, fotoDataList)
        }

        addFotosRequest?.let {
            val call = ApiClient.apiService.addFotos(it)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Fotos uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to upload fotos", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getByteArrayFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
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
        val matrix = android.graphics.Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    private fun encodeImageToBase64(image: ByteArray): String? {
        return Base64.encodeToString(image, Base64.DEFAULT)
    }
}