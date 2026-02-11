package com.example.prueba1integrador

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba1integrador.databinding.ActivityGestionarInventarioBinding
import com.google.firebase.database.FirebaseDatabase

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

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        binding.rvInventarioGestion.layoutManager = LinearLayoutManager(this)
        adapter = GestionarAdapter(
            listaInventario = emptyList(),
            onEditClick = { juego ->
                val intent = Intent(this, EditarInventarioActivity::class.java)
                intent.putExtra("JUEGO", juego)
                startActivity(intent)
            },
            onDeleteClick = { juego, ids ->
                // Pasamos el objeto juego completo que contiene el stock actual
                confirmarEliminacionNueva(juego)
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
        // Mantenemos tu lógica de agrupación por si hay registros duplicados con el mismo nombre
        val agrupados = lista.groupBy { it.nombre.trim().lowercase() }
        val listaVisual = agrupados.map { entry ->
            val listaDeEsteJuego = entry.value
            val juegoRepresentante = listaDeEsteJuego.first()
            val stockTotal = listaDeEsteJuego.sumOf { it.stock }
            val ids = listaDeEsteJuego.map { it.id }
            ItemInventario(juegoRepresentante, stockTotal, ids)
        }
        adapter.actualizarLista(listaVisual)
    }

    private fun confirmarEliminacionNueva(juego: Juego) {
        val opciones = arrayOf("Eliminar 1 unidad", "Eliminar cantidad específica", "Eliminar producto completo")

        AlertDialog.Builder(this)
            .setTitle("Gestionar eliminación: ${juego.nombre}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> ejecutarBaja(juego, 1) // Eliminar 1
                    1 -> mostrarDialogoCantidad(juego) // Cantidad personalizada
                    2 -> ejecutarBaja(juego, juego.stock) // Eliminar todo
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoCantidad(juego: Juego) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Stock actual: ${juego.stock}"

        AlertDialog.Builder(this)
            .setTitle("¿Cuántas unidades eliminar?")
            .setView(input)
            .setPositiveButton("Eliminar") { _, _ ->
                val cantidadStr = input.text.toString()
                val cantidadAEliminar = cantidadStr.toIntOrNull()

                if (cantidadAEliminar != null && cantidadAEliminar > 0) {
                    if (cantidadAEliminar > juego.stock) {
                        Toast.makeText(this, "No puedes eliminar más de lo que hay", Toast.LENGTH_SHORT).show()
                    } else {
                        ejecutarBaja(juego, cantidadAEliminar)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarBaja(juego: Juego, cantidadAEliminar: Int) {
        val nuevoStock = juego.stock - cantidadAEliminar
        val ref = FirebaseDatabase.getInstance().getReference("productos").child(juego.id)

        if (nuevoStock <= 0) {
            // Si el stock llega a 0, eliminamos el registro por completo
            ref.removeValue().addOnSuccessListener {
                inventoryManager.registrarEnHistorial("Admin", "eliminó producto (stock 0)", juego.nombre, 0)
                Toast.makeText(this, "${juego.nombre} eliminado del inventario", Toast.LENGTH_SHORT).show()
                cargarDatos()
            }
        } else {
            // Si todavía queda stock, solo actualizamos el número
            ref.child("stock").setValue(nuevoStock).addOnSuccessListener {
                inventoryManager.registrarEnHistorial("Admin", "redujo stock (-$cantidadAEliminar)", juego.nombre, nuevoStock)
                Toast.makeText(this, "Stock actualizado: $nuevoStock", Toast.LENGTH_SHORT).show()
                cargarDatos()
            }
        }
    }
}