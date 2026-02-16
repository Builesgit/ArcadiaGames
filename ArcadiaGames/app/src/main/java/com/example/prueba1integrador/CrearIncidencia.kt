package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.CrearIncidenciaBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CrearIncidencia : AppCompatActivity() {

    private lateinit var binding: CrearIncidenciaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CrearIncidenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Botón con estilo Amarillo/Ámbar del nuevo XML
        binding.btnRegistrar.setOnClickListener {
            validarYRegistrarIncidencia()
        }
    }

    private fun validarYRegistrarIncidencia() {
        // Los IDs coinciden con el nuevo diseño OutlinedBox
        val tema = binding.textTemaIncidencia.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        var infoAdicional = binding.etInformacion.text.toString().trim()
        val selectedChipId = binding.chipGroupCategorias.checkedChipId

        var esValido = true

        // Validación del Asunto/Tema
        if (tema.isEmpty()) {
            binding.TextLayoutTemaIncidencia.error = "Este campo es obligatorio"
            esValido = false
        } else if (tema.length < 6) {
            binding.TextLayoutTemaIncidencia.error = "Ponga un título más amplio (mín. 6 caracteres)"
            esValido = false
        } else {
            binding.TextLayoutTemaIncidencia.error = null
        }

        // Validación de la Descripción Detallada
        if (descripcion.isEmpty()) {
            binding.TextLayoutDescripcion.error = "Este campo es obligatorio"
            esValido = false
        } else if (descripcion.length < 50) {
            binding.TextLayoutDescripcion.error = "Haga la descripción más descriptiva (mín. 50 caracteres)"
            esValido = false
        } else {
            binding.TextLayoutDescripcion.error = null
        }

        // Info adicional opcional
        if (infoAdicional.isEmpty() || infoAdicional.length < 5) {
            infoAdicional = "### Sin info técnica adicional ###"
        }

        // Obtención del Chip seleccionado (Bug, Usuario, Juego, Otro)
        var tipoIncidencia = ""
        if (selectedChipId == -1) {
            Snackbar.make(binding.root, "Debe seleccionar un tipo de incidencia", Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.Rojo_bug))
                .show()
            esValido = false
        } else {
            val selectedChip = findViewById<Chip>(selectedChipId)
            tipoIncidencia = selectedChip.text.toString()
        }

        if (esValido) {
            registrarEnFirebase(tema, descripcion, infoAdicional, tipoIncidencia)
        }
    }

    private fun registrarEnFirebase(tema: String, descripcion: String, info: String, tipo: String) {
        val userId = auth.currentUser?.uid ?: "Anonimo"
        val userEmail = auth.currentUser?.email ?: "Sin email"
        val reference = database.getReference("incidencias")

        // Recuperamos el nombre para el mensaje personalizado de Arcadia
        database.getReference("usuarios").child(userId).child("nombre").get()
            .addOnSuccessListener { snapshot ->
                val nombreUsuario = snapshot.getValue(String::class.java) ?: "Usuario"
                val id = reference.push().key

                val incidencia = Incidencia(
                    id = id,
                    tema = tema,
                    descripcion = descripcion,
                    infoAdicional = info,
                    usuarioId = userId,
                    usuarioEmail = userEmail,
                    tipo = tipo
                )

                if (id != null) {
                    reference.child(id).setValue(incidencia)
                        .addOnSuccessListener {
                            // --- MENSAJE PERSONALIZADO SOLICITADO ---
                            Toast.makeText(
                                this,
                                "¡Gracias $nombreUsuario! Hemos recibido tu reporte. El equipo de Arcadia lo revisará pronto.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish() // Vuelve al perfil
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error de conexión: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener {
                // Fallback por si no hay internet o no encuentra el nombre
                val id = reference.push().key
                val incidencia = Incidencia(id, tema, descripcion, info, tipo, userId, userEmail)
                id?.let { reference.child(it).setValue(incidencia).addOnSuccessListener { finish() } }
            }
    }
}