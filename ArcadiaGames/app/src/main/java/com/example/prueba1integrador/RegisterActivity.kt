package com.example.prueba1integrador

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// El nombre de la clase Binding siempre es el nombre del XML en CamelCase + "Binding"
import com.example.prueba1integrador.databinding.ActivityRegisterBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        binding.btnRegistrar.setOnClickListener {
            val email = binding.etUsuario.text.toString().trim()
            val pass = binding.etPass.text.toString().trim()
            val confirm = binding.etConfirmPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && pass == confirm) {
                registrarEnFirebase(email, pass)
            } else {
                Toast.makeText(this, "Verifica los campos (correo válido y contraseñas iguales)", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVolverLogin.setOnClickListener { finish() }
    }

    private fun registrarEnFirebase(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: ""

                    // Guardar datos adicionales en Realtime Database
                    val database = Firebase.database
                    val myRef = database.getReference("usuarios").child(uid)

                    val usuarioData = mapOf(
                        "usuario" to email, // Usamos el email como nombre de usuario por defecto
                        "rol" to "user"     // Rol por defecto
                    )

                    myRef.setValue(usuarioData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Registro creado, pero falló al guardar datos: ${it.message}", Toast.LENGTH_LONG).show()
                            finish() // Cerramos igual porque el usuario ya se creó en Auth
                        }
                } else {
                    Toast.makeText(this, "Error en registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}