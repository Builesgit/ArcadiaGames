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

        // Configuración de LayoutManagers
        binding.rvTopVentas.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMenosVistos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Iniciamos la escucha de datos
        obtenerDatosEstadisticos()
    }

    private fun obtenerDatosEstadisticos() {
        // 1. CARGAR TOP VENTAS (Ranking con texto 1º más comprado...)
        // Nota: Asegúrate de que en FirebaseInventoryManager esta función use reversed()
        manager.obtenerTopVentas(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                if (!isAdded || _binding == null) return

                if (lista.isNotEmpty()) {
                    binding.rvTopVentas.adapter = EstadisticasAdapter(lista, true)
                    // El primero tras el reversed() es el número 1 real
                    binding.tvNombreLider.text = " ${lista[0].nombre}"
                } else {
                    binding.tvNombreLider.text = "Sin datos de ventas aún"
                }
            }
        })

        // 2. CARGAR MENOS VISTOS
        manager.obtenerMenosVistos(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                if (!isAdded || _binding == null) return

                if (lista.isNotEmpty()) {
                    binding.rvMenosVistos.adapter = EstadisticasAdapter(lista, false)
                }
            }
        })
    }

    // Importante: Si el script de reconstrucción está funcionando,
    // puedes añadir este método para forzar un refresco manual si lo deseas.
    fun refrescarDashboard() {
        obtenerDatosEstadisticos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}