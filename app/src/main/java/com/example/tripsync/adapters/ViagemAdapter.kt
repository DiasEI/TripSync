package com.example.tripsync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.Trip

class ViagemAdapter(
    private val viagens: List<Trip>,
    private val viewViagemCallback: (Trip) -> Unit,
    private val addLocalCallback: (Trip) -> Unit,
    private val addFotoCallback: (Trip) -> Unit

) : RecyclerView.Adapter<ViagemAdapter.ViagemViewHolder>() {

    inner class ViagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tv_titulo)
        val local: TextView = itemView.findViewById(R.id.location)
        val btnView: ImageButton = itemView.findViewById(R.id.btn_view)
        val btnLocal: ImageButton = itemView.findViewById(R.id.btn_location)
        val btnFoto: ImageButton = itemView.findViewById(R.id.btn_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViagemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_as_minhas_viagens, parent, false)
        return ViagemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViagemViewHolder, position: Int) {
        val viagem = viagens[position]
        holder.titulo.text = viagem.titulo

        "${viagem.pais}, ${viagem.cidade}".also { holder.local.text = it }

        holder.btnView.setOnClickListener {
            viewViagemCallback(viagem)
        }

        holder.btnLocal.setOnClickListener {
            addLocalCallback(viagem)
        }

        holder.btnFoto.setOnClickListener {
            addFotoCallback(viagem)
        }
    }

    override fun getItemCount(): Int {
        return viagens.size
    }
}