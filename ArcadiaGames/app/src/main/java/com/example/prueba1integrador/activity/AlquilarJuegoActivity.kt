package com.example.prueba1integrador.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.prueba1integrador.model.Juego
import com.example.prueba1integrador.databinding.ActivityAlquilarJuegoBinding
import com.example.prueba1integrador.manager.FirebaseInventoryManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AlquilarJuegoActivity : BaseActivity() {

    private lateinit var binding: ActivityAlquilarJuegoBinding
    private val inventoryManager = FirebaseInventoryManager()
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
        } else if (juego?.imagenResId != 0) {
            binding.ivJuego.setImageResource(juego!!.imagenResId)
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

                val minFin = Calendar.getInstance().apply {
                    timeInMillis = fechaInicio!!.timeInMillis
                    add(Calendar.DAY_OF_MONTH, 1)
                }

                mostrarDatePicker(minFin) { calendar ->
                    fechaFin = calendar
                    binding.etFechaFin.setText(formatearFecha(calendar))
                    calcularPrecio()
                }

            } ?: Toast.makeText(this, "Selecciona fecha de inicio", Toast.LENGTH_SHORT).show()
        }


        binding.btnConfirmarAlquiler.setOnClickListener {
            if (juego != null) confirmarAlquiler(juego)
        }
    }

    private fun calcularPrecio() {
        if (fechaInicio == null || fechaFin == null) return

        val dias = TimeUnit.MILLISECONDS.toDays(
            fechaFin!!.timeInMillis - fechaInicio!!.timeInMillis
        )

        val total = if (dias > 0) dias * precioPorDia else 0.0
        binding.tvPrecioTotal.text = "%.2f €".format(total)
    }

    private fun confirmarAlquiler(juego: Juego) {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || fechaInicio == null || fechaFin == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val dias = TimeUnit.MILLISECONDS.toDays(
            fechaFin!!.timeInMillis - fechaInicio!!.timeInMillis
        )

        if (fechaFin!!.timeInMillis == fechaInicio!!.timeInMillis) {
            Toast.makeText(
                this,
                "La fecha de inicio y fin no pueden ser el mismo día",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (fechaFin!!.timeInMillis < fechaInicio!!.timeInMillis) {
            Toast.makeText(
                this,
                "La fecha de fin no puede ser anterior a la fecha de inicio",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val precioFinal = dias * precioPorDia
        val uid = user.uid

        val db = FirebaseDatabase.getInstance().reference
        val alquilerId = db.child("alquileres").child(uid).push().key ?: return

        val alquilerMap = hashMapOf(
            "id" to juego.id,
            "nombre" to juego.nombre,
            "plataforma" to juego.plataforma,
            "precio" to "%.2f €".format(precioFinal),
            "imagenUrl" to juego.imagenUrl,
            "imagenResId" to juego.imagenResId,
            "fechaInicio" to fechaInicio!!.timeInMillis,
            "fechaFin" to fechaFin!!.timeInMillis,
            "timestamp" to System.currentTimeMillis()
        )



        db.child("alquileres")
            .child(uid)
            .child(alquilerId)
            .setValue(alquilerMap)
            .addOnSuccessListener {

                inventoryManager.registrarEnHistorial(user.email ?: "Usuario", "Alquiló juego", juego.nombre, 1)
                Toast.makeText(this, "Alquiler realizado correctamente ", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar alquiler", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDatePicker(minDate: Calendar, onDate: (Calendar) -> Unit) {
        val c = Calendar.getInstance()
        val dpd = DatePickerDialog(
            this,
            { _, y, m, d ->
                val res = Calendar.getInstance().apply { set(y, m, d) }
                onDate(res)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        dpd.datePicker.minDate = minDate.timeInMillis
        dpd.show()
    }

    private fun formatearFecha(c: Calendar): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c.time)
    }
}
