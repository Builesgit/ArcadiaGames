package com.example.prueba1integrador

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
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
        super.attachBaseContext(LanguageUtils.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.flagSpanish.setOnClickListener { changeLang("es") }
        binding.flagEnglish.setOnClickListener { changeLang("en") }
        binding.flagFrench.setOnClickListener { changeLang("fr") }

        binding.btnRegistrar.setOnClickListener {

            limpiarErrores()

            val nombreUser = binding.etNombreUsuario.text.toString().trim()
            val email = binding.etUsuario.text.toString().trim()
            val pass = binding.etPass.text.toString().trim()
            val confirm = binding.etConfirmPass.text.toString().trim()

            if (!validarCampos(nombreUser, email, pass, confirm)) return@setOnClickListener

            comprobarNombreUnicoYRegistrar(nombreUser, email, pass)
        }

        binding.btnVolverLogin.setOnClickListener { finish() }
    }

    // =========================
    // VALIDACIONES
    // =========================
    private fun validarCampos(nombre: String, email: String, pass: String, confirm: String): Boolean {

        if (nombre.isEmpty()) {
            binding.lyNombreUsuario.error = "Introduce tu nombre de usuario"
            return false
        }

        if (email.isEmpty()) {
            binding.lyUsuario.error = "Introduce un correo electrónico"
            return false
        }

        if (!email.contains("@gmail.com")) {
            binding.lyUsuario.error = "El correo debe contener @gmail.com"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.lyUsuario.error = "Formato de correo inválido"
            return false
        }

        // 🔐 Validación avanzada contraseña
        val regexPassword = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,12}$")

        if (!regexPassword.matches(pass)) {
            binding.lyPass.error =
                "Debe tener 8-12 caracteres, una mayúscula y un símbolo"
            return false
        }

        if (pass != confirm) {
            binding.lyConfirmPass.error = "Las contraseñas no coinciden"
            return false
        }

        return true
    }

    private fun comprobarNombreUnicoYRegistrar(nombre: String, email: String, pass: String) {

        val refUsuarios = Firebase.database.getReference("usuarios")

        refUsuarios.get().addOnSuccessListener { snapshot ->

            for (userSnap in snapshot.children) {

                val nombreExistente = userSnap.child("nombre")
                    .getValue(String::class.java)

                if (nombreExistente.equals(nombre, ignoreCase = true)) {

                    binding.lyNombreUsuario.error = "Este nombre ya está registrado"
                    return@addOnSuccessListener   // 🔥 CORTA EJECUCIÓN AQUÍ
                }
            }

            // 🔥 SOLO SI NO EXISTE SE REGISTRA
            registrarEnFirebase(email, pass, nombre)

        }.addOnFailureListener {

            Toast.makeText(this, "Error verificando nombre", Toast.LENGTH_SHORT).show()
        }
    }
    private fun limpiarErrores() {
        binding.lyNombreUsuario.error = null
        binding.lyUsuario.error = null
        binding.lyPass.error = null
        binding.lyConfirmPass.error = null
    }

    // =========================
    // REGISTRO FIREBASE
    // =========================
    private fun registrarEnFirebase(email: String, pass: String, nombre: String) {

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid ?: ""
                    val myRef = Firebase.database.getReference("usuarios").child(uid)

                    val usuarioData = mapOf(
                        "nombre" to nombre,
                        "correo" to email,
                        "rol" to "user"
                    )

                    myRef.setValue(usuarioData).addOnSuccessListener {
                        Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                } else {

                    when (task.exception) {

                        is FirebaseAuthUserCollisionException -> {
                            binding.lyUsuario.error = "Este correo ya está registrado"
                        }

                        else -> {
                            Toast.makeText(
                                this,
                                "Error: ${task.exception?.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }

    private fun changeLang(code: String) {
        LanguageUtils.saveLocale(this, code)
        recreate()
    }
}