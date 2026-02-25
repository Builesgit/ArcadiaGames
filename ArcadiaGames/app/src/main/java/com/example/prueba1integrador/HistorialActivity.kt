package com.example.prueba1integrador

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityHistorialBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*

class HistorialActivity : BaseActivity() {
    private lateinit var binding: ActivityHistorialBinding
    private var listaCompleta = mutableListOf<AccionHistorial>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración simple del RecyclerView
        binding.rvHistorial.layoutManager = LinearLayoutManager(this)

        // Carga de datos original
        val ref = FirebaseDatabase.getInstance().getReference("historial")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCompleta.clear()
                for (data in snapshot.children) {
                    data.getValue(AccionHistorial::class.java)?.let { listaCompleta.add(it) }
                }
                listaCompleta.reverse()
                filtrarLista(binding.tabFiltroHistorial.selectedTabPosition)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.tabFiltroHistorial.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filtrarLista(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filtrarLista(posicion: Int) {
        val listaFiltrada = if (posicion == 0) {
            listaCompleta.filter {
                it.accion.contains("añadió", true) ||
                        it.accion.contains("eliminó", true) ||
                        it.accion.contains("actualizó", true) ||
                        it.accion.contains("stock", true)
            }
        } else {
            listaCompleta.filter {
                it.accion.contains("compró", ignoreCase = true) ||
                        it.accion.contains("intercambió", ignoreCase = true) ||
                        it.accion.contains("alquiló", ignoreCase = true)
            }
        }
        binding.rvHistorial.adapter = HistorialAdapter(listaFiltrada)
    }
}