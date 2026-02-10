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

        // Configuración del botón para añadir incidencia
        // Nota: El ID 'cardAñadir' en XML se convierte a 'cardAadir' en ViewBinding
        binding.cardAnadir.setOnClickListener {
            val intent = Intent(requireContext(), CrearIncidencia::class.java)
            startActivity(intent)
        }

        // Configuración del RecyclerView con datos vacíos o dummy por ahora
        binding.recyclerViewAdmin.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AdminAdapter(emptyList()) { _ ->
            // Acción vacía por ahora
        }
        binding.recyclerViewAdmin.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}