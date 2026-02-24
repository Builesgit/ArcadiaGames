package com.example.prueba1integrador

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityGestionarInventarioBinding

class GestionarInventarioActivity : BaseActivity() {

    private lateinit var binding: ActivityGestionarInventarioBinding
    private val inventoryManager = FirebaseInventoryManager()
    private lateinit var adapter: GestionarAdapter
    private var listaVisualInventario: List<ItemInventario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)

        adapter = GestionarAdapter(
            listaInventario = listaVisualInventario,
            onEditClick = { /* Lógica de edición si fuera necesaria */ },
            onDataChanged = { cargarDatos() }
        )
        binding.rvInventarioGestion.adapter = adapter
    }

    private fun cargarDatos() {
        binding.progressBarGestion.visibility = View.VISIBLE
        inventoryManager.consultarInventario(object : FirebaseInventoryManager.InventoryCallback {
            override fun onDataLoaded(lista: List<Juego>) {
                binding.progressBarGestion.visibility = View.GONE
                procesarYMostrarLista(lista)
            }
        })
    }

    private fun procesarYMostrarLista(lista: List<Juego>) {
        val agrupados = lista.groupBy { it.nombre.trim().lowercase() }

        listaVisualInventario = agrupados.map { entry ->
            val listaDeEsteJuego = entry.value
            val juegoRepresentante = listaDeEsteJuego.first()

            // 1. Calculamos el stock por plataforma primero
            val stockPS = listaDeEsteJuego.sumOf { it.detalle_stock?.get("playstation") ?: 0 }
            val stockXB = listaDeEsteJuego.sumOf { it.detalle_stock?.get("xbox") ?: 0 }
            val stockNI = listaDeEsteJuego.sumOf { it.detalle_stock?.get("nintendo") ?: 0 }
            val stockPC = listaDeEsteJuego.sumOf { it.detalle_stock?.get("pc") ?: 0 }

            // 2. La cantidad total DEBE ser la suma de los stocks individuales para ser coherente
            val sumaTotalReal = stockPS + stockXB + stockNI + stockPC

            ItemInventario(
                juego = juegoRepresentante,
                cantidad = sumaTotalReal, // Aquí usamos la suma real del detalle
                idsAgrupados = listaDeEsteJuego.map { it.id },
                ps = stockPS,
                xb = stockXB,
                ni = stockNI,
                pc = stockPC
            )
        }
        adapter.actualizarLista(listaVisualInventario)
    }
}