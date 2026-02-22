package com.example.prueba1integrador

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        super.attachBaseContext(LanguageUtils.updateBaseContextLocale(newBase))
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedLang = prefs.getString("My_Lang", "es") ?: "es"
        val currentLang = resources.configuration.locales.get(0).language

        // Si el idioma cambió mientras estábamos en otra pantalla, refrescamos
        if (currentLang != savedLang) {
            recreate()
        }
    }
}