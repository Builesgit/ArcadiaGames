package com.example.prueba1integrador

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

        reemplazarFragmento(FragmentHome())
        setupNavigation()
        setupRailViews()

        // El mando púrpura abre y cierra el menú
        binding.btnAbrirRail.setOnClickListener {
            if (binding.navigationRail.visibility == View.VISIBLE) {
                binding.navigationRail.visibility = View.GONE
            } else {
                binding.navigationRail.visibility = View.VISIBLE
            }
        }

        // Cerrar al tocar en la zona de juegos
        binding.mainHomeUFragment.setOnClickListener {
            binding.navigationRail.visibility = View.GONE
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_menu -> reemplazarFragmento(FragmentHome())
                R.id.item_catalogo -> reemplazarFragmento(FragmentCatalogo())
                R.id.item_perfil -> reemplazarFragmento(FragmentPerfil())
                else -> false
            }
            true
        }
    }

    private fun setupRailViews() {
        val inflater = LayoutInflater.from(this)

        // 1. Añadir el Header (Logo)
        val headerView = inflater.inflate(R.layout.rail_header, binding.navigationRail, false)
        binding.navigationRail.addHeaderView(headerView)

        // 2. Añadir el Footer (Información)
        // IMPORTANTE: NO usamos addView, usamos un contenedor que el Rail gestione o
        // simplemente lo inflamos como footer si tu versión lo permite.
        // Si addFooterView te dio error antes, usaremos este método seguro:
        val footerView = inflater.inflate(R.layout.rail_footer, binding.navigationRail, false)

        // En lugar de MATCH_PARENT, usamos WRAP_CONTENT y gravedad inferior
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }

        binding.navigationRail.addView(footerView, params)

        footerView.findViewById<ImageButton>(R.id.btn_info_uso)?.setOnClickListener {
            reemplazarFragmento(FragmentGuiaUso())
        }
    }

    private fun reemplazarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_home_U_fragment, fragmento)
            .commit()
    }
}