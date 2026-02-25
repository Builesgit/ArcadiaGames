package com.example.prueba1integrador

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
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

        if (savedInstanceState == null) {
            reemplazarFragmento(FragmentHome())
        }

        setupNavigation(rol)
        setupRailViews()

        binding.btnAbrirRail.setOnClickListener {
            if (binding.navigationRail.visibility == View.VISIBLE) {
                cerrarMenuLateral()
            } else {
                abrirMenuLateral()
            }
        }

        binding.viewScrim.setOnClickListener {
            cerrarMenuLateral()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedLang = prefs.getString("My_Lang", "es") ?: "es"
        val currentLang = resources.configuration.locales.get(0).language

        if (currentLang != savedLang) {
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
            binding.navigationRail.menu.findItem(R.id.item_estadisticas)?.isVisible = true
        } else {
            binding.navigationRail.menu.findItem(R.id.item_estadisticas)?.isVisible = false
        }

        binding.navigationRail.setOnItemSelectedListener { item ->
            cerrarMenuLateral()
            when (item.itemId) {
                R.id.item_menu -> { reemplazarFragmento(FragmentHome()); true }
                R.id.item_catalogo -> { reemplazarFragmento(FragmentCatalogo()); true }
                R.id.item_estadisticas -> { if (rol == "admin") reemplazarFragmento(FragmentEstadisticas()); true }
                R.id.item_cesta -> { if (rol != "admin") reemplazarFragmento(FragmentCesta()); true }
                R.id.item_perfil -> { reemplazarFragmento(FragmentPerfil()); true }
                else -> false
            }
        }
    }

    private fun setupRailViews() {
        val inflater = LayoutInflater.from(this)
        val headerView = inflater.inflate(R.layout.rail_header, binding.navigationRail, false)
        binding.navigationRail.addHeaderView(headerView)

        val footerView = inflater.inflate(R.layout.rail_footer, binding.navigationRail, false)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.BOTTOM
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