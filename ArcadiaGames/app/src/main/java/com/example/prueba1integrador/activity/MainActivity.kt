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

    // ESTO ES LO MÁS IMPORTANTE: Obliga a la actividad a usar el idioma guardado
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

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedLang = prefs.getString("My_Lang", "es") ?: "es"
        val currentLang = resources.configuration.locales.get(0).language

        if (currentLang != savedLang) {
            recreate()
        }
    }

    // =========================
    // VALIDACIONES
    // =========================
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
            Toast.makeText(
                this,
                "Contraseña inválida",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }

    private fun limpiarErrores() {
        // Si tu login usa TextInputLayout podemos limpiar errores aquí
    }

    // =========================
    // LOGIN FIREBASE
    // =========================
    private fun loginConFirebase(email: String, pass: String) {

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser
                    val dbRef = Firebase.database.getReference("usuarios")
                        .child(user?.uid ?: "")

                    dbRef.get().addOnSuccessListener { snapshot ->

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

                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_LONG).show()
                        }

                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
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