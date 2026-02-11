package com.example.prueba1integrador

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.prueba1integrador.databinding.ActivityAlquilarJuegoBinding
import java.text.SimpleDateFormat
import java.util.*
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

        val juego = intent.getSerializableExtra("JUEGO") as? Juego
        binding.tvNombreJuego.text = juego?.nombre ?: "Juego"

        if (juego?.imagenUrl?.isNotEmpty() == true) {
            Glide.with(this).load(juego.imagenUrl).into(binding.ivJuego)
        }

        setupListeners(juego)
    }

    private fun setupListeners(juego: Juego?) {
        binding.etFechaInicio.setOnClickListener {
            mostrarDatePicker(Calendar.getInstance()) { calendar ->
                fechaInicio = calendar
                binding.etFechaInicio.setText(formatearFecha(calendar))
            }
        }

        binding.etFechaFin.setOnClickListener {
            fechaInicio?.let {
                mostrarDatePicker(it) { calendar ->
                    fechaFin = calendar
                    binding.etFechaFin.setText(formatearFecha(calendar))
                    calcularPrecio()
                }
            } ?: Toast.makeText(this, "Selecciona fecha de inicio", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirmarAlquiler.setOnClickListener { confirmarAlquiler(juego) }
    }

    private fun calcularPrecio() {
        if (fechaInicio == null || fechaFin == null) return
        val dias = TimeUnit.MILLISECONDS.toDays(fechaFin!!.timeInMillis - fechaInicio!!.timeInMillis)
        val total = if (dias > 0) dias * precioPorDia else 0.0
        binding.tvPrecioTotal.text = "%.2f €".format(total)
    }

    private fun confirmarAlquiler(juego: Juego?) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || juego == null || fechaInicio == null || fechaFin == null) return

        val precioFinal = (TimeUnit.MILLISECONDS.toDays(fechaFin!!.timeInMillis - fechaInicio!!.timeInMillis)) * precioPorDia

        val alquiler = Alquiler(
            alquilerId = UUID.randomUUID().toString(),
            juegoId = juego.id,
            nombreJuego = juego.nombre,
            idUsuario = user.uid,
            nombreUsuario = binding.etNombreCliente.text.toString(),
            fechaInicio = formatearFecha(fechaInicio!!),
            fechaFin = formatearFecha(fechaFin!!),
            precioTotal = precioFinal,
            timestamp = System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("alquileres").push().setValue(alquiler)
            .addOnSuccessListener {
                registrarEnHistorial(juego.nombre, user.email ?: "Usuario")
                Toast.makeText(this, "Alquiler exitoso", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun registrarEnHistorial(juego: String, usuario: String) {
        val log = AccionHistorial(
            usuarioNombre = usuario,
            accion = "alquiló",
            productoNombre = juego,
            fecha = System.currentTimeMillis()
        )
        FirebaseDatabase.getInstance().getReference("historial").push().setValue(log)
    }

    private fun mostrarDatePicker(minDate: Calendar, onDate: (Calendar) -> Unit) {
        val c = Calendar.getInstance()
        val dpd = DatePickerDialog(this, { _, y, m, d ->
            val res = Calendar.getInstance().apply { set(y, m, d) }
            onDate(res)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.minDate = minDate.timeInMillis
        dpd.show()
    }

    private fun formatearFecha(c: Calendar) = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c.time)
}