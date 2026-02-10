package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.prueba1integrador.databinding.ActivityEditarInventarioBinding
import com.google.firebase.database.FirebaseDatabase

class EditarInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarInventarioBinding
    private lateinit var juego: Juego

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Recibimos el juego a editar
        val juegoRecibido = intent.getSerializableExtra("JUEGO") as? Juego
        if (juegoRecibido == null) {
            Toast.makeText(this, "Error al cargar el juego", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        juego = juegoRecibido

        rellenarCampos()
        setupListeners()
    }

    /**
     * Rellena los campos (los bloqueados y el stock)
     */
    private fun rellenarCampos() {

        binding.etCategoria.setText(juego.categoria)
        binding.etPlataforma.setText(juego.plataforma)
        binding.etNombreJuego.setText(juego.nombre)
        binding.etPrecio.setText(juego.precio)
        binding.etStock.setText(juego.stock.toString())

        // Cargar imagen
        if (juego.imagenUrl.isNotEmpty()) {
            Glide.with(this)
                .load(juego.imagenUrl)
                .into(binding.ivImagenJuego)
        } else if (juego.imagenResId != 0) {
            binding.ivImagenJuego.setImageResource(juego.imagenResId)
        }
    }

    /**
     * Listener del botón Guardar
     */
    private fun setupListeners() {

        binding.btnGuardarStock.setOnClickListener {
            guardarStock()
        }
    }

    /**
     * Actualiza SOLO el stock en Firebase
     */
    private fun guardarStock() {

        val stockTexto = binding.etStock.text.toString().trim()

        if (stockTexto.isEmpty()) {
            Toast.makeText(this, "Introduce un stock válido", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoStock = stockTexto.toIntOrNull()
        if (nuevoStock == null || nuevoStock < 0) {
            Toast.makeText(this, "El stock no puede ser negativo", Toast.LENGTH_SHORT).show()
            return
        }

        val stockRef = FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(juego.id)
            .child("stock")

        stockRef.setValue(nuevoStock)
            .addOnSuccessListener {
                Toast.makeText(this, "Stock actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
