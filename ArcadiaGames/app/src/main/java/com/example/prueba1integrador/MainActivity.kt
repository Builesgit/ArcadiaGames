package com.example.prueba1integrador

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prueba1integrador.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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

        // Banderas
        binding.flagSpanish.setOnClickListener { changeLang("es") }
        binding.flagEnglish.setOnClickListener { changeLang("en") }
        binding.flagFrench.setOnClickListener { changeLang("fr") }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtUsuario.text.toString().trim()
            val pass = binding.edtPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginConFirebase(email, pass)
            } else {
                Toast.makeText(this, getString(R.string.completar_campos), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Al volver del registro, si el idioma cambió, recreamos la actividad
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedLang = prefs.getString("My_Lang", "es") ?: "es"
        val currentLang = resources.configuration.locales.get(0).language

        if (currentLang != savedLang) {
            recreate()
        }
    }

    private fun changeLang(code: String) {
        LanguageUtils.saveLocale(this, code)
        recreate()
    }

    private fun loginConFirebase(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val dbRef = Firebase.database.getReference("usuarios").child(user?.uid ?: "")

                    dbRef.get().addOnSuccessListener { snapshot ->
                        val rol = snapshot.child("rol").value?.toString() ?: "cliente"
                        val nombreUsuario = snapshot.child("nombre").value?.toString() ?: email

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("ROL_USUARIO", rol)
                        intent.putExtra("USUARIO_LOGUEADO", nombreUsuario)

                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error de seguridad", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                }
            }
    }
}