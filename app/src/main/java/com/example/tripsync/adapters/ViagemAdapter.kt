package com.example.tripsync.adapters

import android.app.Activity
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
    private val activity: Activity,
    private val deleteViagemCallback: (Trip) -> Unit,
    private val editViagemCallback: (Trip) -> Unit,
    private val viewViagemCallback: (Trip) -> Unit // Add this parameter
) : RecyclerView.Adapter<ViagemAdapter.ViagemViewHolder>() {

    inner class ViagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tv_titulo)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViagemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_as_minhas_viagens, parent, false)
        return ViagemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViagemViewHolder, position: Int) {
        val viagem = viagens[position]
        holder.titulo.text = viagem.titulo

        holder.titulo.setOnClickListener {
            viewViagemCallback(viagem) // Handle item click
        }

        holder.btnEdit.setOnClickListener {
            editViagemCallback(viagem)
        }

        holder.btnDelete.setOnClickListener {
            deleteViagemCallback(viagem)
        }
    }

    override fun getItemCount(): Int {
        return viagens.size
    }
}