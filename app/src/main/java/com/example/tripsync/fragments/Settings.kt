package com.example.tripsync.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.tripsync.ChangeLanguageActivity
import com.example.tripsync.LoginActivity
import com.example.tripsync.R
import com.example.tripsync.ViagensActivity

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
            val perfilFragment = Perfil()
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_perfil, perfilFragment) // substitua R.id.fragment_container pelo ID do contêiner de fragmento correto
            transaction.addToBackStack(null)
            transaction.commit()
        }


        linguagemButton.setOnClickListener {
            val intent = Intent(requireContext(), ChangeLanguageActivity::class.java)
            startActivity(intent)
        }


        viagensButton.setOnClickListener {
            val intent = Intent(requireContext(), ViagensActivity::class.java)
            startActivity(intent)
        }

        offButton.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
