package com.example.tripsync.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.tripsync.R
import com.example.tripsync.adapters.AsMinhasViagensAdapter
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import com.example.tripsync.api.User
import com.example.tripsync.fragments.placeholder.PlaceholderContent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import kotlin.collections.ArrayList

/**
 * A fragment representing a list of Items.
 */
class AsMinhasViagensFragment : Fragment() {

    private var columnCount = 1
    private lateinit var recyclerView:RecyclerView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_as_minhas_viagens, container, false)
        recyclerView = view.findViewById(R.id.list);

        loadViagens()

        return view
    }

    fun loadAdapter(trips : ArrayList<Trip>) {
        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = AsMinhasViagensAdapter(trips)
            }
        }
    }

    private fun loadViagens() {
        val sharedPreferences = requireActivity().getSharedPreferences("ViagensPrefs", Context.MODE_PRIVATE)
        var userId = sharedPreferences.getString("userId", null)
        var token = sharedPreferences.getString("token", null)

        if (userId != null && token != null) {
            ApiClient.apiService.getViagensByUser(userId!!).enqueue(object : Callback<ArrayList<Trip>> {
                override fun onResponse(call: Call<ArrayList<Trip>>, response: Response<ArrayList<Trip>>) {
                    if (response.isSuccessful) {
                        var trips = ArrayList<Trip>()

                        // METER O CONTGEUDO NA LISTA


                        loadAdapter(trips)
                    } else {
                        Toast.makeText(requireContext(), "Failed to load trip", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ArrayList<Trip>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            AsMinhasViagensFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

}