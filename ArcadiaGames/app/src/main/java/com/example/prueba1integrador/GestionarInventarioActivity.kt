package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityGestionarInventarioBinding

class GestionarInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionarInventarioBinding
    private val inventoryManager = FirebaseInventoryManager()
    private lateinit var adapter: GestionarAdapter
    private var listaVisualInventario: List<ItemInventario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // 🔥 Actualiza precios silenciosamente
        inventoryManager.actualizarPreciosSegunPlataforma { }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)
        adapter = GestionarAdapter(
            listaInventario = listaVisualInventario,
            onEditClick = { juego ->
                val intent = Intent(this, EditarInventarioActivity::class.java)
                intent.putExtra("JUEGO_EDITAR", juego)
                startActivity(intent)
            },
            onDataChanged = { cargarDatos() }
        )
        binding.rvInventarioGestion.adapter = adapter
    }

    private fun cargarDatos() {
        binding.progressBarGestion.visibility = View.VISIBLE
        inventoryManager.consultarInventario(object :
            FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                binding.progressBarGestion.visibility = View.GONE
                procesarYMostrarLista(lista)
            }
        })
    }

    private fun procesarYMostrarLista(lista: List<Juego>) {

        val listaVisual = lista.groupBy { it.nombre.trim().lowercase() }
            .map { (_, listaDeEsteJuego) ->

                val juegoRepresentante = listaDeEsteJuego.first()
                val stockTotal = listaDeEsteJuego.sumOf { it.stock }
                val ids = listaDeEsteJuego.map { it.id }

                ItemInventario(juegoRepresentante, stockTotal, ids)
            }

        if (::adapter.isInitialized) {
            adapter.actualizarLista(listaVisual)
        }
    }
}
