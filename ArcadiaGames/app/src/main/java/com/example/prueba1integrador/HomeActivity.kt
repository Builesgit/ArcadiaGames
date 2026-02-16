package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.prueba1integrador.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rol = intent.getStringExtra("ROL_USUARIO") ?: "cliente"

        // Inicialización
        reemplazarFragmento(FragmentHome())
        setupNavigation(rol)
        setupRailViews()

        // 1. Lógica del botón de la barra lateral para abrir/cerrar
        binding.btnAbrirRail.setOnClickListener {
            if (binding.navigationRail.visibility == View.VISIBLE) {
                cerrarMenuLateral()
            } else {
                abrirMenuLateral()
            }
        }

        // 2. LA CLAVE: Si el Scrim es visible y se pulsa, se cierra el menú.
        // Esto cubre cualquier parte de la pantalla fuera del Rail.
        binding.viewScrim.setOnClickListener {
            cerrarMenuLateral()
        }
    }

    // --- FUNCIONES DE CONTROL DE INTERFAZ ---

    private fun abrirMenuLateral() {
        binding.navigationRail.visibility = View.VISIBLE
        binding.viewScrim.visibility = View.VISIBLE

        // ORDEN CORRECTO DE CAPAS:
        // Primero el scrim para que tape el fragmento
        binding.viewScrim.bringToFront()

        // Segundo el menú para que quede ENCIMA del scrim y sea clicable
        binding.navigationRail.bringToFront()

        // Por último el botón por si quieres volver a pulsarlo
        binding.btnAbrirRail.bringToFront()
    }

    private fun cerrarMenuLateral() {
        binding.navigationRail.visibility = View.GONE
        binding.viewScrim.visibility = View.GONE
    }

    private fun setupNavigation(rol: String) {
        if (rol == "admin") {
            val menu = binding.navigationRail.menu
            menu.findItem(R.id.item_cesta)?.isVisible = false
        }

        binding.navigationRail.setOnItemSelectedListener { item ->
            // Cerramos el rail y el scrim al seleccionar una opción
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

        // Header
        val headerView = inflater.inflate(R.layout.rail_header, binding.navigationRail, false)
        binding.navigationRail.addHeaderView(headerView)

        // Footer manual
        val footerView = inflater.inflate(R.layout.rail_footer, binding.navigationRail, false)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.BOTTOM
        binding.navigationRail.addView(footerView, params)

        footerView.findViewById<ImageButton>(R.id.btn_info_uso)?.setOnClickListener {
            reemplazarFragmento(FragmentGuiaUso())
            cerrarMenuLateral() // También cerramos aquí
        }
    }

    private fun reemplazarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_home_U_fragment, fragmento)
            .commit()
    }
}