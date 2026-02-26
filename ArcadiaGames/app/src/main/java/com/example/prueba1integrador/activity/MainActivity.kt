package com.example.prueba1integrador.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.prueba1integrador.utils.LanguageUtils
import com.example.prueba1integrador.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageUtils.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.flagSpanish.setOnClickListener { changeLang("es") }
        binding.flagEnglish.setOnClickListener { changeLang("en") }
        binding.flagFrench.setOnClickListener { changeLang("fr") }

        binding.btnLogin.setOnClickListener {
            limpiarErrores()
            val email = binding.edtUsuario.text.toString().trim()
            val pass = binding.edtPassword.text.toString().trim()

            if (!validarCampos(email, pass)) return@setOnClickListener

            loginConFirebase(email, pass)
        }

        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // ELIMINADO EL ONRESUME: No hace falta comprobar el idioma aquí,
    // al usar recreate() en changeLang, la actividad ya se recarga sola.

    private fun validarCampos(email: String, pass: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Introduce tu correo", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!email.contains("@")) {
            Toast.makeText(this, "El correo debe contener @gmail.com", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Formato de correo inválido", Toast.LENGTH_SHORT).show()
            return false
        }
        val regexPassword = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,12}$")
        if (!regexPassword.matches(pass)) {
            Toast.makeText(this, "Contraseña inválida", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun limpiarErrores() { }

    private fun loginConFirebase(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Firebase.database.getReference("usuarios").child(user?.uid ?: "").get().addOnSuccessListener { snapshot ->
                        val rol = snapshot.child("rol").value?.toString() ?: "user"
                        val nombreUsuario = snapshot.child("nombre").value?.toString() ?: email

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", rol)
                        intent.putExtra("USUARIO_LOGUEADO", nombreUsuario)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_LONG).show()
                        is FirebaseAuthInvalidCredentialsException -> Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(this, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun changeLang(code: String) {
        LanguageUtils.saveLocale(this, code)

        // Creamos un intent explícito para reiniciar la actividad
        val intent = Intent(this, MainActivity::class.java)

        // Estos flags aseguran que la actividad se limpie y se recree desde cero
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)

        // Esta es la parte que "suaviza" el cambio evitando el salto brusco
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        finish()
    }
}