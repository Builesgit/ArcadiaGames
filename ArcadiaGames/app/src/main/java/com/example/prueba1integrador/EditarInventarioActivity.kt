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

        // 1. Recibimos el juego enviado desde el GestionarAdapter
        val juegoRecibido = intent.getSerializableExtra("JUEGO") as? Juego
        if (juegoRecibido == null) {
            Toast.makeText(this, "Error: No se pudo cargar la información del producto", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        juego = juegoRecibido

        // 2. Rellenamos la interfaz con los datos actuales
        rellenarCampos()

        // 3. Configuramos el botón de guardado
        setupListeners()
    }

    private fun rellenarCampos() {
        // Bloqueamos los campos que no son stock para mantener integridad (según el diseño de tu compañera)
        binding.etNombreJuego.setText(juego.nombre)
        binding.etNombreJuego.isEnabled = false

        binding.etCategoria.setText(juego.categoria)
        binding.etCategoria.isEnabled = false

        binding.etPlataforma.setText(juego.plataforma)
        binding.etPlataforma.isEnabled = false

        binding.etPrecio.setText(juego.precio)
        binding.etPrecio.isEnabled = false

        // El stock es el único campo editable aquí
        binding.etStock.setText(juego.stock.toString())

        // Carga de imagen con Glide (Mejora técnica de tu compañera)
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
            Toast.makeText(this, "Introduce un número de stock válido (0 o superior)", Toast.LENGTH_SHORT).show()
            return
        }

        // Referencia directa al nodo del producto
        val productoRef = FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(juego.id)

        // Actualizamos solo el campo stock
        productoRef.child("stock").setValue(nuevoStock)
            .addOnSuccessListener {
                // --- TU SISTEMA DE HISTORIAL ---
                // Registramos la acción para que aparezca en tu Dashboard de Admin
                inventoryManager.registrarEnHistorial(
                    nombreUser = "Admin",
                    accion = "actualizó stock (${juego.stock} -> $nuevoStock)",
                    producto = juego.nombre
                )

                Toast.makeText(this, "Stock actualizado correctamente ✅", Toast.LENGTH_SHORT).show()
                finish() // Volvemos al inventario
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar en Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}