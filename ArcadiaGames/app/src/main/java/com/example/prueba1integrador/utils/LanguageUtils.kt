package com.example.prueba1integrador.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageUtils {
    fun updateBaseContextLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "es") ?: "es"

        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    fun saveLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit().putString("My_Lang", languageCode).apply()
    }
}