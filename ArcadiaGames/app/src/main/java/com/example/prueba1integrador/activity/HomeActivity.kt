package com.example.prueba1integrador.activity

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.prueba1integrador.fragment.FragmentCatalogo
import com.example.prueba1integrador.fragment.FragmentCesta
import com.example.prueba1integrador.fragment.FragmentGuiaUso
import com.example.prueba1integrador.fragment.FragmentHome
import com.example.prueba1integrador.fragment.FragmentPerfil
import com.example.prueba1integrador.utils.LanguageUtils
import com.example.prueba1integrador.R
import com.example.prueba1integrador.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageUtils.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rol = intent.getStringExtra("ROL_USUARIO") ?: "cliente"

        // Configuración inicial
        if (savedInstanceState == null) {
            reemplazarFragmento(FragmentHome())
        }

        setupNavigation(rol)
        setupRailViews()

        // Botón para abrir el Navigation Rail
        binding.btnAbrirRail.setOnClickListener {
            if (binding.navigationRail.visibility == View.VISIBLE) {
                cerrarMenuLateral()
            } else {
                abrirMenuLateral()
            }
        }

        // Fondo oscuro para cerrar el menú
        binding.viewScrim.setOnClickListener {
            cerrarMenuLateral()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedLang = prefs.getString("My_Lang", "es") ?: "es"

        // Obtenemos el idioma que tiene la actividad en este momento
        val currentLang = resources.configuration.locales.get(0).language

        if (currentLang != savedLang) {
            // Si no coinciden, forzamos el reinicio de la actividad para aplicar el idioma
            recreate()
        }
    }

    private fun abrirMenuLateral() {
        binding.navigationRail.visibility = View.VISIBLE
        binding.viewScrim.visibility = View.VISIBLE

        binding.viewScrim.bringToFront()
        binding.navigationRail.bringToFront()
        binding.btnAbrirRail.bringToFront()
    }

    private fun cerrarMenuLateral() {
        binding.navigationRail.visibility = View.GONE
        binding.viewScrim.visibility = View.GONE
    }

    private fun setupNavigation(rol: String) {
        if (rol == "admin") {
            binding.navigationRail.menu.findItem(R.id.item_cesta)?.isVisible = false
        }

        binding.navigationRail.setOnItemSelectedListener { item ->
            cerrarMenuLateral()

            when (item.itemId) {
                R.id.item_menu -> {
                    reemplazarFragmento(FragmentHome())
                    true
                }
                R.id.item_catalogo -> {
                    reemplazarFragmento(FragmentCatalogo())
                    true
                }
                R.id.item_cesta -> {
                    if (rol != "admin") {
                        reemplazarFragmento(FragmentCesta())
                    }
                    true
                }
                R.id.item_perfil -> {
                    reemplazarFragmento(FragmentPerfil())
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRailViews() {
        val inflater = LayoutInflater.from(this)

        // Cabecera (Logo/Usuario)
        val headerView = inflater.inflate(R.layout.rail_header, binding.navigationRail, false)
        binding.navigationRail.addHeaderView(headerView)

        // Pie de página (Botón de Ayuda/Guía)
        val footerView = inflater.inflate(R.layout.rail_footer, binding.navigationRail, false)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM
        binding.navigationRail.addView(footerView, params)

        footerView.findViewById<ImageButton>(R.id.btn_info_uso)?.setOnClickListener {
            reemplazarFragmento(FragmentGuiaUso())
            cerrarMenuLateral()
        }
    }

    private fun reemplazarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_home_U_fragment, fragmento)
            .commit()
    }
}