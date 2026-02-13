package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.FragmentHomeABinding

class FragmentHomeA : Fragment() {

    private var _binding: FragmentHomeABinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeABinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Botón para ir a la Activity de Crear Incidencia
        // Usamos el ID cardAnadir que está presente en tu XML
        binding.cardAnadir.setOnClickListener {
            val intent = Intent(requireContext(), CrearIncidencia::class.java)
            startActivity(intent)
        }


        binding.cardGestionDeIncidencias.setOnClickListener {
            val fragmentoIncidencias = FragmentMostrarIncidencias()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                // CAMBIA 'main_home_U_fragment' POR EL ID DEL CONTENEDOR DE TU ACTIVITY
                .replace(R.id.main_home_U_fragment, fragmentoIncidencias)
                .addToBackStack(null)
                .commit()
        }

        // 3. Configuración del RecyclerView del Admin
        binding.recyclerViewAdmin.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AdminAdapter(emptyList()) { _ -> }
        binding.recyclerViewAdmin.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}