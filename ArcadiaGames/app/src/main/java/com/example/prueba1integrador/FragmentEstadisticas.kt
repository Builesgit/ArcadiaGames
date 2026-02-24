package com.example.prueba1integrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.FragmentEstadisticasBinding

class FragmentEstadisticas : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!
    private val manager = FirebaseInventoryManager()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTopVentas.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMenosVistos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        obtenerDatosEstadisticos()
    }

    private fun obtenerDatosEstadisticos() {
        // 1. CARGAR TOP VENTAS (Usa la función del manager)
        manager.obtenerTopVentas(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                if (!isAdded) return
                binding.rvTopVentas.adapter = EstadisticasAdapter(lista)

                // Actualizar el líder visualmente
                if (lista.isNotEmpty()) {
                    binding.tvNombreLider.text = lista[0].nombre
                }
            }
        })

        // 2. CARGAR MENOS VISTOS (Usa la otra función)
        manager.obtenerMenosVistos(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                if (!isAdded) return
                binding.rvMenosVistos.adapter = EstadisticasAdapter(lista)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}