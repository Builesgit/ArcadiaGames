package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
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

        val usuario = intent.getStringExtra("USUARIO_LOGUEADO") ?: "Invitado"
        val rol = intent.getStringExtra("ROL_USUARIO") ?: "user"

        setupNavigationGlobal(usuario, rol)
    }

    private fun setupNavigationGlobal(usuario: String, rol: String) {
        cargarFragmento(FragmentHome())

        val headerView = binding.navigationRail.getHeaderView()
        val btnMenuHeader = headerView?.findViewById<ImageButton>(R.id.btn_expandir)

        // Configuración inicial: barra oculta
        binding.navigationRail.visibility = View.GONE

        // Botón "Hamburguesa" de la Activity (el pequeño que queda fuera)
        binding.btnAbrirRail.setOnClickListener {
            binding.navigationRail.visibility = View.VISIBLE
            binding.btnAbrirRail.visibility = View.GONE
        }

        // Botón del Header (el que está dentro de la barra lateral)
        btnMenuHeader?.setOnClickListener {
            binding.navigationRail.visibility = View.GONE
            binding.btnAbrirRail.visibility = View.VISIBLE
        }

        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_menu -> cargarFragmento(FragmentHome())
                R.id.item_catalogo -> cargarFragmento(FragmentCatalogo())
                R.id.item_perfil -> cargarFragmento(FragmentPerfil.newInstance(usuario, rol))
                R.id.item_cesta -> startActivity(Intent(this, CestaActivity::class.java))
            }
            // Cerrar menú al navegar para evitar que tape la vista
            binding.navigationRail.visibility = View.GONE
            binding.btnAbrirRail.visibility = View.VISIBLE
            true
        }
    }

    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_home_U_fragment, fragment)
            .commit()
    }
}