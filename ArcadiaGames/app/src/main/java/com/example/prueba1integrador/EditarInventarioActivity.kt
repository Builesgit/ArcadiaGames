package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityEditarInventarioBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class EditarInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarInventarioBinding
    private lateinit var juego: Juego
    private val inventoryManager = FirebaseInventoryManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibimos el juego enviado desde el GestionarAdapter con la clave correcta
        val juegoRecibido = intent.getSerializableExtra("JUEGO_EDITAR") as? Juego
        if (juegoRecibido == null) {
            Toast.makeText(this, "Error: No se pudo cargar la información", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        juego = juegoRecibido
        rellenarCampos()
        setupListeners()
    }

    private fun rellenarCampos() {
        binding.etNombreJuego.setText(juego.nombre)
        binding.etNombreJuego.isEnabled = false
        binding.etCategoria.setText(juego.categoria)
        binding.etCategoria.isEnabled = false
        binding.etPlataforma.setText(juego.plataforma)
        binding.etPlataforma.isEnabled = false
        binding.etPrecio.setText(juego.precio)
        binding.etPrecio.isEnabled = false

        binding.etStock.setText(juego.stock.toString())

        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(this)
                .load(juego.imagenUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.ivImagenJuego)
        }
    }

    private fun setupListeners() {
        binding.btnGuardarStock.setOnClickListener {
            actualizarStockFirebase()
        }
    }

    private fun actualizarStockFirebase() {
        val stockTexto = binding.etStock.text.toString().trim()
        val nuevoStock = stockTexto.toIntOrNull()

        if (nuevoStock == null || nuevoStock < 0) {
            Toast.makeText(this, "Introduce un número de stock válido", Toast.LENGTH_SHORT).show()
            return
        }

        val productoRef = FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(juego.id)

        productoRef.child("stock").setValue(nuevoStock)
            .addOnSuccessListener {
                // CORRECCIÓN: Usamos 'cant' que es como lo tienes en tu FirebaseInventoryManager
                inventoryManager.registrarEnHistorial(
                    nombreUser = "Admin",
                    accion = "actualizó stock",
                    producto = juego.nombre,
                    cant = nuevoStock
                )

                Toast.makeText(this, "Stock actualizado correctamente ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}