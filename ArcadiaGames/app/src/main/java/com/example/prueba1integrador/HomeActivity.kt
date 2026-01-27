package com.example.prueba1integrador

import android.os.Bundle
import android.view.View
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
        val rol = intent.getStringExtra("ROL") ?: "user"

        setupNavigationGlobal(usuario, rol)
    }

    private fun setupNavigationGlobal(usuario: String, rol: String) {
        val density = resources.displayMetrics.density

        // 1. Pantalla por defecto al entrar
        cargarFragmento(FragmentHome())

        // 2. Control de clics en la barra lateral
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_menu -> cargarFragmento(FragmentHome())
                R.id.item_catalogo -> cargarFragmento(FragmentCatalogo())
                R.id.item_perfil -> {
                    // Para el perfil, puedes cargarlo como Fragment o como Activity.
                    // Si quieres que la barra siga ahí, conviértelo en Fragment.
                    cargarFragmento(FragmentPerfil.newInstance(usuario, rol))
                }
            }
            // Reset visual de la barra tras elegir
            binding.navigationRail.layoutParams.width = 0
            binding.navigationRail.requestLayout()
            binding.btnAbrirRail.translationX = 0f
            true
        }

        // 3. Botón para abrir la barra (Hamburguesa)
        binding.btnAbrirRail.setOnClickListener {
            binding.navigationRail.visibility = View.VISIBLE
            binding.navigationRail.layoutParams.width = (72 * density).toInt()
            binding.navigationRail.requestLayout()
            binding.btnAbrirRail.translationX = -100 * density
        }
    }

    // Función para cambiar de vista sin cerrar la barra lateral
    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_home_U_fragment, fragment)
            .commit()
    }
}