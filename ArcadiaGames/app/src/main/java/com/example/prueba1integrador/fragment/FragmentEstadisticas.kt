package com.example.prueba1integrador.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.R
import com.example.prueba1integrador.adapter.EstadisticasAdapter
import com.example.prueba1integrador.databinding.FragmentEstadisticasBinding
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.example.prueba1integrador.model.Juego

class FragmentEstadisticas : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!
    private val manager = FirebaseInventoryManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración de los RecyclerViews en horizontal para el Dashboard
        binding.rvTopVentas.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvMenosVistos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        cargarDatosEstadisticos()
    }

    private fun cargarDatosEstadisticos() {
        binding.tvNombreLider.text = "Consultando..."

        // 1. CARGAR TOP VENTAS
        manager.obtenerTopVentas(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                // Verificamos que el fragmento siga existiendo para evitar crasheos
                if (!isAdded || _binding == null) return

                if (lista.isNotEmpty()) {
                    // Pasamos 'true' para indicar que es ranking de ventas (usará color verde)
                    binding.rvTopVentas.adapter = EstadisticasAdapter(lista, true)
                    binding.tvNombreLider.text = lista[0].nombre
                } else {
                    binding.tvNombreLider.text = "Sin registros"
                }
            }
        })

        // 2. CARGAR MENOS VISTOS
        manager.obtenerMenosVistos(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                if (!isAdded || _binding == null) return

                if (lista.isNotEmpty()) {
                    // Pasamos 'false' para indicar que es ranking de vistas (usará color ámbar)
                    binding.rvMenosVistos.adapter = EstadisticasAdapter(lista, false)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}