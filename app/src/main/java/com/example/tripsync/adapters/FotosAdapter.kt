package com.example.tripsync.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripsync.R
import com.example.tripsync.RegisterActivity.Companion.MAX_DIMENSION
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.min

class FotosAdapter(private val fotos: MutableList<Uri>, private val context: Context) :
    RecyclerView.Adapter<FotosAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_foto, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val fotoUri = fotos[position]
        val fotoByteArray = getByteArrayFromUri(fotoUri)
        if (fotoByteArray != null) {
            val imageData = Base64.encodeToString(fotoByteArray, Base64.DEFAULT)
            Glide.with(holder.itemView.context)
                .load("data:image/jpeg;base64,$imageData")
                .into(holder.imageView)
        } else {
        }
    }

    override fun getItemCount(): Int {
        return fotos.size
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    private fun getByteArrayFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
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
        val scale = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val matrix = android.graphics.Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }
}
