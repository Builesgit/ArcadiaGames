package com.example.prueba1integrador

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityGestionarInventarioBinding

class GestionarInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionarInventarioBinding
    private val inventoryManager = FirebaseInventoryManager()
    private lateinit var adapter: GestionarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    // Se ejecuta cada vez que volvemos a esta pantalla
    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)

        adapter = GestionarAdapter(
            listaInventario = emptyList(),
            onDeleteClick = { juego, idsAgrupados ->
                confirmarEliminacion(juego, idsAgrupados)
            }
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

        val listaVisual = lista.map { juego ->
            ItemInventario(
                juego = juego,
                cantidad = juego.stock,
                idsAgrupados = listOf(juego.id)
            )
        }

        adapter.actualizarLista(listaVisual)
    }

    private fun confirmarEliminacion(juego: Juego, idsAgrupados: List<String>) {

        val mensaje = if (idsAgrupados.size > 1) {
            "Hay ${idsAgrupados.size} unidades de '${juego.nombre}'. ¿Quieres eliminar UNA unidad o TODAS?"
        } else {
            "¿Estás seguro de que quieres eliminar '${juego.nombre}'?"
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage(mensaje)
            .setPositiveButton("Eliminar UNO") { _, _ ->
                if (idsAgrupados.isNotEmpty()) {
                    eliminarJuegoPorId(idsAgrupados.first())
                }
            }
            .setNegativeButton("Cancelar", null)

        if (idsAgrupados.size > 1) {
            builder.setNeutralButton("Eliminar TODOS") { _, _ ->
                var procesados = 0
                for (id in idsAgrupados) {
                    inventoryManager.eliminarProducto(id, object :
                        FirebaseInventoryManager.DeleteCallback {

                        override fun onDeleteComplete(exito: Boolean) {
                            procesados++
                            if (procesados == idsAgrupados.size) {
                                Toast.makeText(
                                    this@GestionarInventarioActivity,
                                    "Productos eliminados correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                cargarDatos()
                            }
                        }
                    })
                }
            }
        }

        builder.show()
    }

    private fun eliminarJuegoPorId(id: String) {
        inventoryManager.eliminarProducto(id, object :
            FirebaseInventoryManager.DeleteCallback {

            override fun onDeleteComplete(exito: Boolean) {
                if (exito) {
                    Toast.makeText(
                        this@GestionarInventarioActivity,
                        "Producto eliminado (Stock -1)",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarDatos()
                } else {
                    Toast.makeText(
                        this@GestionarInventarioActivity,
                        "Error al eliminar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}
