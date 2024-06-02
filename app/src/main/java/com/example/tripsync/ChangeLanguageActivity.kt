package com.example.tripsync


import androidx.appcompat.app.AppCompatActivity


import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import java.util.*

class ChangeLanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.language)

        val ptRadio = findViewById<RadioButton>(R.id.ptRadio)
        val enRadio = findViewById<RadioButton>(R.id.enRadio)
        val btn_guard = findViewById<Button>(R.id.btn_guard)

        btn_guard.setOnClickListener {
            when {
                ptRadio.isChecked -> setLocale("pt")
                enRadio.isChecked -> setLocale("en")
            }
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        recreate()  // Recreate activity to apply the language change
    }
}