package com.example.tripsync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.api.Trip

class ViagemAdapter(private val viagens: List<Trip>) : RecyclerView.Adapter<ViagemAdapter.ViagemViewHolder>() {

    class ViagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val descricao: TextView = itemView.findViewById(R.id.tvDescricao)
        val cidade: TextView = itemView.findViewById(R.id.tvCidade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViagemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_viagem, parent, false)
        return ViagemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViagemViewHolder, position: Int) {
        val viagem = viagens[position]
        holder.titulo.text = viagem.titulo
        holder.descricao.text = viagem.descricao
        holder.cidade.text = viagem.cidade
    }

    override fun getItemCount(): Int {
        return viagens.size
    }
}
