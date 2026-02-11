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

        // Recuperamos el rol para saber si ocultar la cesta
        val rol = intent.getStringExtra("ROL_USUARIO") ?: "cliente"

        reemplazarFragmento(FragmentHome())
        setupNavigation(rol)
        setupRailViews()

        // El mando púrpura abre y cierra el menú
        binding.btnAbrirRail.setOnClickListener {
            binding.navigationRail.visibility = if (binding.navigationRail.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // Cerrar al tocar en la zona de fragmentos
        binding.mainHomeUFragment.setOnClickListener {
            binding.navigationRail.visibility = View.GONE
        }
    }

    private fun setupNavigation(rol: String) {
        // --- LOGICA PARA OCULTAR LA CESTA A ADMINS ---
        if (rol == "admin") {
            val menu = binding.navigationRail.menu
            val itemCesta = menu.findItem(R.id.item_cesta)
            itemCesta?.isVisible = false
        }

        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_menu -> {
                    reemplazarFragmento(FragmentHome())
                    true
                }
                R.id.item_catalogo -> {
                    reemplazarFragmento(FragmentCatalogo())
                    true
                }
                R.id.item_perfil -> {
                    reemplazarFragmento(FragmentPerfil())
                    true
                }
                R.id.item_cesta -> {
                    // Solo permitimos abrir la cesta si NO es admin
                    if (rol != "admin") {
                        startActivity(Intent(this, CestaActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRailViews() {
        val inflater = LayoutInflater.from(this)

        // 1. Añadir el Header (Logo)
        val headerView = inflater.inflate(R.layout.rail_header, binding.navigationRail, false)
        binding.navigationRail.addHeaderView(headerView)

        // 2. Añadir el Footer (Botón de Info)
        val footerView = inflater.inflate(R.layout.rail_footer, binding.navigationRail, false)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }

        binding.navigationRail.addView(footerView, params)

        footerView.findViewById<ImageButton>(R.id.btn_info_uso)?.setOnClickListener {
            reemplazarFragmento(FragmentGuiaUso())
            binding.navigationRail.visibility = View.GONE // Cerramos el rail tras elegir
        }
    }

    private fun reemplazarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_home_U_fragment, fragmento)
            .commit()
    }
}