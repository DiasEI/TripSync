package com.example.tripsync.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.tripsync.LoginActivity
import com.example.tripsync.R

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
        val offButton = view.findViewById<ImageButton>(R.id.offButtton)

        //Set click listeners
        perfilButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            transaction.replace(R.id.frame_layout, Perfil())
            transaction.commit()
        }

        linguagemButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            transaction.replace(R.id.frame_layout, ChangeLanguage())
            transaction.commit()
        }

        offButton.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
