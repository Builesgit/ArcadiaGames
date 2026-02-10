package com.example.prueba1integrador

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityHistorialBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialBinding
    private var listaCompleta = mutableListOf<AccionHistorial>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistorial.layoutManager = LinearLayoutManager(this)

        val ref = FirebaseDatabase.getInstance().getReference("historial")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCompleta.clear()
                for (data in snapshot.children) {
                    data.getValue(AccionHistorial::class.java)?.let { listaCompleta.add(it) }
                }
                listaCompleta.reverse()
                // Por defecto mostramos Admins (Tab 0)
                filtrarLista(0)
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
            // Filtrar acciones realizadas por Admins
            listaCompleta.filter { it.accion.contains("añadió") || it.accion.contains("eliminó") }
        } else {
            // Filtrar acciones realizadas por Usuarios
            listaCompleta.filter { it.accion.contains("compró") || it.accion.contains("intercambió") }
        }
        binding.rvHistorial.adapter = HistorialAdapter(listaFiltrada)
    }
}