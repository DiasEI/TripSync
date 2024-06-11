package com.example.tripsync.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.tripsync.api.Trip
import com.example.tripsync.databinding.FragmentItemAsMinhasViagensBinding

import com.example.tripsync.fragments.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class AsMinhasViagensAdapter(
    private val values: List<Trip>
) : RecyclerView.Adapter<AsMinhasViagensAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemAsMinhasViagensBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.titulo

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemAsMinhasViagensBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber


        override fun toString(): String {
            return super.toString() + " '" + idView.text + "'"
        }
    }

}