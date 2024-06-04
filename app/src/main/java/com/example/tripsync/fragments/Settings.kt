package com.example.tripsync.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.tripsync.Activity3
import com.example.tripsync.ChangeLanguageActivity
import com.example.tripsync.R
import com.example.tripsync.perfilActivity

class Settings: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val perfilButton = view.findViewById<ImageButton>(R.id.perfilButtton)
        val linguagemButton = view.findViewById<ImageButton>(R.id.linguagemButtton)
        val viagensButton = view.findViewById<ImageButton>(R.id.viagensButtton)
        val offButton = view.findViewById<ImageButton>(R.id.offButtton)

        //Set click listeners
        perfilButton.setOnClickListener {
            val intent = Intent(requireContext(), perfilActivity::class.java)
            startActivity(intent)
        }

        linguagemButton.setOnClickListener {
            // Open ChangeLanguageActivity
            val intent = Intent(requireContext(), ChangeLanguageActivity::class.java)
            startActivity(intent)
        }


        //  viagensButton.setOnClickListener {
            // Handle viagensButton click
        //   }

        //   offButton.setOnClickListener {
            // Handle offButton click
            // }
    }
}
