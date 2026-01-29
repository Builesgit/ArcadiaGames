package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prueba1integrador.databinding.ActivityPerfilBinding

class FragmentPerfil : Fragment() {

    private var _binding: ActivityPerfilBinding? = null
    private val binding get() = _binding!!

    companion object {
        /**
         * Crea una nueva instancia del fragmento pasando el usuario y su rol.
         */
        fun newInstance(usuario: String, rol: String): FragmentPerfil {
            val fragment = FragmentPerfil()
            val args = Bundle()
            args.putString("USUARIO", usuario)
            args.putString("ROL", rol)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Se utiliza el binding para inflar activity_perfil.xml
        _binding = ActivityPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperamos los datos del Bundle
        val usuario = arguments?.getString("USUARIO") ?: "Desconocido"
        val rol = arguments?.getString("ROL") ?: "user"

        // Configuración visual básica según los datos recibidos
        binding.txtNombreUsuario.text = usuario
        binding.txtTituloPerfil.text = if (rol == "admin") "ADMINISTRADOR" else "USUARIO"

        // Lógica de visibilidad de los bloques de botones
        if (rol == "admin") {
            binding.layoutAdmin.visibility = View.VISIBLE
            binding.layoutUsuario.visibility = View.GONE

            // Acción para el botón de Administrador - Crear Producto
            binding.btnCrearProducto.setOnClickListener {
                val intent = Intent(requireContext(), AnadirProductoActivity::class.java)
                startActivity(intent)
            }

            // Acción para el botón de Administrador - Gestionar Inventario
            binding.btnInventario.setOnClickListener {
                startActivity(Intent(requireContext(), GestionarInventarioActivity::class.java))
            }
        } else {
            binding.layoutAdmin.visibility = View.GONE
            binding.layoutUsuario.visibility = View.VISIBLE

            // Acción para el botón de Usuario (Subir Juego)
            binding.btnSubirJuego.setOnClickListener {
                val intent = Intent(requireContext(), AnadirProductoActivity::class.java)
                startActivity(intent)
            }
        }

        // Lógica para cerrar sesión
        binding.btnLogout.setOnClickListener {
            // Suponiendo que MainActivity es tu pantalla de Login/Inicio
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para evitar fugas de memoria
        _binding = null
    }
}