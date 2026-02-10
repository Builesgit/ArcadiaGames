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

        binding.btnRegistrar.setOnClickListener {
            validarYRegistrarIncidencia()
        }
    }

    private fun validarYRegistrarIncidencia() {
        val tema = binding.textTemaIncidencia.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        var infoAdicional = binding.etInformacion.text.toString().trim()
        val selectedChipId = binding.chipGroupCategorias.checkedChipId

        var esValido = true

        // Validación del tema (ID en XML: TextLayout_Tema_Incidencia -> CamelCase: textLayoutTemaIncidencia)
        if (tema.isEmpty()) {
            binding.TextLayoutTemaIncidencia.error = "Este campo es obligatorio"
            esValido = false
        } else if (tema.length < 6) {
            binding.TextLayoutTemaIncidencia.error = "Ponga un título más amplio (mín. 6 caracteres)"
            esValido = false
        } else {
            binding.TextLayoutTemaIncidencia.error = null
        }

        // Validación de la descripción (ID en XML: TextLayout_descripcion -> CamelCase: textLayoutDescripcion)
        if (descripcion.isEmpty()) {
            binding.TextLayoutDescripcion.error = "Este campo es obligatorio"
            esValido = false
        } else if (descripcion.length < 50) {
            binding.TextLayoutDescripcion.error = "Haga la descripción más descriptiva (mín. 50 caracteres)"
            esValido = false
        } else {
            binding.TextLayoutDescripcion.error = null
        }

        // Validación de info adicional
        if (infoAdicional.length < 10) {
            infoAdicional = "### Sin más info adicional ###"
            binding.etInformacion.setText(infoAdicional)
        }

        // Validación de Chips
        var tipoIncidencia = ""
        if (selectedChipId == -1) {
            Snackbar.make(binding.root, "Debe seleccionar un tipo de incidencia", Snackbar.LENGTH_LONG).show()
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
        val reference = database.getReference("incidencias")
        val id = reference.push().key

        val incidencia = Incidencia(
            id = id,
            tema = tema,
            descripcion = descripcion,
            infoAdicional = info,
            tipo = tipo,
            usuarioId = userId
        )

        if (id != null) {
            reference.child(id).setValue(incidencia)
                .addOnSuccessListener {
                    Toast.makeText(this, "Incidencia registrada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar la incidencia: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}