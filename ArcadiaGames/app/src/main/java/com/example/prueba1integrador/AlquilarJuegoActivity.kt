package com.example.prueba1integrador

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.prueba1integrador.databinding.ActivityAlquilarJuegoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AlquilarJuegoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlquilarJuegoBinding

    private val precioPorDia = 5.0

    private var fechaInicio: Calendar? = null
    private var fechaFin: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlquilarJuegoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibimos el juego desde el popup
        val juego = intent.getSerializableExtra("JUEGO") as? Juego
        binding.tvNombreJuego.text = juego?.nombre ?: "Juego"

        if (juego?.imagenUrl?.isNotEmpty() == true) {
            Glide.with(this)
                .load(juego.imagenUrl)
                .into(binding.ivJuego)
        }

        setupListeners()
    }

    private fun setupListeners() {

        binding.etFechaInicio.setOnClickListener {
            mostrarDatePicker(minDate = Calendar.getInstance()) { calendar ->
                fechaInicio = calendar
                binding.etFechaInicio.setText(formatearFecha(calendar))

                // Reset de fecha fin si ya no es válida
                fechaFin?.let {
                    if (!it.after(calendar)) {
                        fechaFin = null
                        binding.etFechaFin.text = null
                        binding.tvPrecioTotal.text = "Precio total (€)"
                    }
                }
            }
        }

        binding.etFechaFin.setOnClickListener {
            if (fechaInicio == null) {
                Toast.makeText(this, "Selecciona primero la fecha de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minFechaFin = fechaInicio!!.clone() as Calendar

            mostrarDatePicker(minDate = minFechaFin) { calendar ->
                fechaFin = calendar
                binding.etFechaFin.setText(formatearFecha(calendar))
                calcularPrecio()
            }
        }

        binding.btnConfirmarAlquiler.setOnClickListener {
            confirmarAlquiler()
        }
    }

    private fun mostrarDatePicker(
        minDate: Calendar,
        onFechaSeleccionada: (Calendar) -> Unit
    ) {
        val calendario = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fecha = Calendar.getInstance()
                fecha.set(year, month, dayOfMonth)
                onFechaSeleccionada(fecha)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        // 🔒 Bloquea fechas anteriores
        datePicker.datePicker.minDate = minDate.timeInMillis

        datePicker.show()
    }

    private fun calcularPrecio() {
        if (fechaInicio == null || fechaFin == null) return

        val diferenciaMillis = fechaFin!!.timeInMillis - fechaInicio!!.timeInMillis
        val dias = TimeUnit.MILLISECONDS.toDays(diferenciaMillis)

        if (dias == 0L) {
            Toast.makeText(
                this,
                "No se puede alquilar un juego con la misma fecha de inicio y fin",
                Toast.LENGTH_SHORT
            ).show()
            binding.tvPrecioTotal.text = "Precio total (€)"
            return
        }

        if (dias < 0) {
            binding.tvPrecioTotal.text = "Precio total (€)"
            return
        }

        val precioTotal = dias * precioPorDia
        binding.tvPrecioTotal.text = String.format(Locale.US, "%.2f €", precioTotal)
    }


    private fun formatearFecha(calendar: Calendar): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(calendar.time)
    }

    private fun confirmarAlquiler() {

        val nombreCliente = binding.etNombreCliente.text.toString().trim()
        if (nombreCliente.isEmpty()) {
            Toast.makeText(this, "Introduce tu nombre y apellidos", Toast.LENGTH_SHORT).show()
            return
        }

        val juego = intent.getSerializableExtra("JUEGO") as? Juego
        if (juego == null) {
            Toast.makeText(this, "Error: juego no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        if (fechaInicio == null || fechaFin == null) {
            Toast.makeText(this, "Selecciona las fechas de alquiler", Toast.LENGTH_SHORT).show()
            return
        }

        val diferenciaMillis = fechaFin!!.timeInMillis - fechaInicio!!.timeInMillis
        val dias = TimeUnit.MILLISECONDS.toDays(diferenciaMillis)

        if (dias <= 0) {
            Toast.makeText(this, "Fechas no válidas", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔐 USUARIO AUTENTICADO
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val idUsuario = currentUser.uid
        val precioTotal = dias * precioPorDia

        val alquilerRef = FirebaseDatabase.getInstance()
            .getReference("alquileres")
            .push()

        val alquiler = Alquiler(
            alquilerId = alquilerRef.key ?: "",
            juegoId = juego.id,
            nombreJuego = juego.nombre,
            idUsuario = idUsuario,                 // 👈 AQUÍ SE GUARDA
            nombreUsuario = nombreCliente,
            fechaInicio = formatearFecha(fechaInicio!!),
            fechaFin = formatearFecha(fechaFin!!),
            dias = dias,
            precioTotal = precioTotal,
            timestamp = System.currentTimeMillis()
        )

        alquilerRef.setValue(alquiler)
            .addOnSuccessListener {
                Toast.makeText(this, "Alquiler registrado correctamente", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


}
