package com.example.prueba1integrador.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.prueba1integrador.utils.LanguageUtils
import com.example.prueba1integrador.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context) {
        // Mantenemos la lógica de idioma
        super.attachBaseContext(LanguageUtils.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.flagSpanish.setOnClickListener { changeLang("es") }
        binding.flagEnglish.setOnClickListener { changeLang("en") }
        binding.flagFrench.setOnClickListener { changeLang("fr") }

        binding.btnRegistrar.setOnClickListener {
            val nombreUser = binding.etNombreUsuario.text.toString().trim()
            val email = binding.etUsuario.text.toString().trim()
            val pass = binding.etPass.text.toString().trim()
            val confirm = binding.etConfirmPass.text.toString().trim()

            if (!validarCampos(nombreUser, email, pass, confirm)) return@setOnClickListener

            comprobarNombreUnicoYRegistrar(nombreUser, email, pass)
        }

        binding.btnVolverLogin.setOnClickListener { finish() }
    }

    private fun validarCampos(nombre: String, email: String, pass: String, confirm: String): Boolean {
        limpiarErrores()

        if (nombre.isBlank()) {
            binding.lyNombreUsuario.error = "Introduce tu nombre de usuario"
            return false
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.lyUsuario.error = "Correo inválido"
            return false
        }

        val regexPassword = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,12}$")
        if (!regexPassword.matches(pass)) {
            binding.lyPass.error = "8-12 caracteres, 1 mayúscula y 1 símbolo"
            return false
        }

        if (pass != confirm) {
            binding.lyConfirmPass.error = "Las contraseñas no coinciden"
            return false
        }

        return true
    }

    private fun comprobarNombreUnicoYRegistrar(nombre: String, email: String, pass: String) {
        Firebase.database.getReference("usuarios").get().addOnSuccessListener { snapshot ->
            for (userSnap in snapshot.children) {
                if (userSnap.child("nombre").getValue(String::class.java).equals(nombre, ignoreCase = true)) {
                    binding.lyNombreUsuario.error = "Este nombre ya está registrado"
                    return@addOnSuccessListener
                }
            }
            registrarEnFirebase(email, pass, nombre)
        }.addOnFailureListener {
            Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registrarEnFirebase(email: String, pass: String, nombre: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                val usuarioData = mapOf("nombre" to nombre, "correo" to email, "rol" to "user")

                Firebase.database.getReference("usuarios").child(uid).setValue(usuarioData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            } else {
                if (task.exception is FirebaseAuthUserCollisionException) {
                    binding.lyUsuario.error = "Este correo ya está registrado"
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun limpiarErrores() {
        binding.lyNombreUsuario.error = null
        binding.lyUsuario.error = null
        binding.lyPass.error = null
        binding.lyConfirmPass.error = null
    }

    private fun changeLang(code: String) {
        LanguageUtils.saveLocale(this, code)

        // Estandarizamos el reinicio
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        // Aplicamos la misma transición de fundido
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}