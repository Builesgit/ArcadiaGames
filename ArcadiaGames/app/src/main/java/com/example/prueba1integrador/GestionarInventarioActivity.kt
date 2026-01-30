package com.example.prueba1integrador

import android.content.Intent
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
        // cargarDatos()  Se quita para que no se llame doble al iniciar
    }

    // El método onResume se ejecuta CADA VEZ que la pantalla vuelve a estar visible
    override fun onResume() {
        super.onResume()
        cargarDatos() // Esto refresca la lista al volver de AnadirProductoActivity
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)

        adapter = GestionarAdapter(emptyList(),
            onEditClick = { juego ->
                // Abrir AnadirProductoActivity pasando el objeto juego para editar
                val intent = Intent(this, AnadirProductoActivity::class.java)
                intent.putExtra("JUEGO_A_EDITAR", juego)
                startActivity(intent)
            },
            onDeleteClick = { juego, idsAgrupados ->
                confirmarEliminacion(juego, idsAgrupados)
            }
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
        // Agrupamos por nombre (normalizado) para contar duplicados
        val agrupados = lista.groupBy { it.nombre.trim().lowercase() }

        val listaVisual = agrupados.map { entry ->
            val listaDeEsteJuego = entry.value
            val juegoRepresentante = listaDeEsteJuego.first()
            val cantidad = listaDeEsteJuego.size
            val ids = listaDeEsteJuego.map { it.id }

            ItemInventario(juegoRepresentante, cantidad, ids)
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
                // Eliminar solo el primer ID disponible (el más antiguo o cualquiera)
                // Esto simula "reducir stock en 1"
                if (idsAgrupados.isNotEmpty()) {
                    eliminarJuegoPorId(idsAgrupados.first(), esEliminacionMasiva = false)
                }
            }
            .setNegativeButton("Cancelar", null)

        if (idsAgrupados.size > 1) {
            builder.setNeutralButton("Eliminar TODOS") { _, _ ->
                // Para eliminar todos, controlamos cuando termine el último para refrescar
                var procesados = 0
                for (id in idsAgrupados) {
                    inventoryManager.eliminarProducto(id, object : FirebaseInventoryManager.DeleteCallback {
                        override fun onDeleteComplete(exito: Boolean) {
                            procesados++
                            // Cuando hayamos procesado todos los IDs, refrescamos la lista
                            if (procesados == idsAgrupados.size) {
                                Toast.makeText(this@GestionarInventarioActivity, "Productos eliminados correctamente", Toast.LENGTH_SHORT).show()
                                cargarDatos()
                            }
                        }
                    })
                }
            }
        }

        builder.show()
    }

    private fun eliminarJuegoPorId(id: String, esEliminacionMasiva: Boolean) {
        inventoryManager.eliminarProducto(id, object : FirebaseInventoryManager.DeleteCallback {
            override fun onDeleteComplete(exito: Boolean) {
                if (exito) {
                    if (!esEliminacionMasiva) {
                        Toast.makeText(this@GestionarInventarioActivity, "Producto eliminado (Stock -1)", Toast.LENGTH_SHORT).show()
                        cargarDatos() // Refrescar la lista tras borrar uno solo
                    }
                } else {
                    Toast.makeText(this@GestionarInventarioActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}