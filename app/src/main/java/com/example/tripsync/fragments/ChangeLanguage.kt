package com.example.tripsync.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.tripsync.R
import java.util.*

class ChangeLanguage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_language, container, false)

        val ptRadio = view.findViewById<RadioButton>(R.id.ptRadio)
        val enRadio = view.findViewById<RadioButton>(R.id.enRadio)
        val btn_guard = view.findViewById<Button>(R.id.btn_guard)
        val btn_voltar = view.findViewById<ImageButton>(R.id.btn_voltar)

        btn_guard.setOnClickListener {
            when {
                ptRadio.isChecked -> setLocale("pt")
                enRadio.isChecked -> setLocale("en")
            }
        }

        btn_voltar.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            transaction.replace(R.id.frame_layout, Settings())
            transaction.commit()
        }

        return view
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().recreate()
    }
}
