package com.example.tripsync.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripsync.R
import com.example.tripsync.adapters.ViagemAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListViagens : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viagemAdapter: ViagemAdapter
    private var viagens: MutableList<Trip> = mutableListOf()
    private var userId: String? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_as_minhas_viagens, container, false)

        recyclerView = view.findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viagemAdapter = ViagemAdapter(viagens, this::viewViagem, this::addLocal ,this::addFoto)
        recyclerView.adapter = viagemAdapter

        val sharedPreferences = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)
        token = sharedPreferences.getString("token", null)

        if (userId != null && token != null) {
            loadViagens()
        } else {
            Toast.makeText(activity, getString(R.string.errorUser), Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadViagens() {
        ApiClient.apiService.getViagensByUser(userId!!).enqueue(object : Callback<List<Trip>> {
            override fun onResponse(call: Call<List<Trip>>, response: Response<List<Trip>>) {
                if (response.isSuccessful) {
                    viagens.clear()
                    viagens.addAll(response.body()!!)
                    viagemAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(activity, getString(R.string.errorViagem), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addLocal(viagem: Trip) {
        val fragment = AddLocal()
        val args = Bundle()
        args.putString("tripId", viagem.id_viagem)
        fragment.arguments = args

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun addFoto(viagem: Trip) {
        val fragment = AddFoto()
        val args = Bundle()
        args.putString("tripId", viagem.id_viagem)
        fragment.arguments = args

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun viewViagem(viagem: Trip) {
        val fragment = Viagens()
        val args = Bundle()
        args.putString("tripId", viagem.id_viagem)
        fragment.arguments = args

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}